package chatHack.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import chatHack.context.ServerContext;
import chatHack.context.Context;
import chatHack.context.PrivateClientContext;
import chatHack.frame.PrivateMsgFrame;

public class ClientChatHack {

	private static Logger logger = Logger.getLogger(ClientChatHack.class.getName());

	private final SocketChannel sc;
	private SocketAddress socketAddress;
	private final Selector selector;
	private SelectionKey serverKey;

	private Thread mainThread;
	private Thread readThread;
	private Thread cnxThread;

	private final String ip;
	private final int port;
	private final String path;
	private final String login;
	private final String password;
	private final boolean withPassword;

	private final Map<String, Client> clients = new HashMap<>();

	private final BlockingQueue<String> sources = new LinkedBlockingQueue<>();
	private boolean cnxRequest = false;
	private int portForPrivate = -1;
	private ServerSocketChannel ssc;

	private final Object monitor = new Object();

	private String lastDst;
	private long lastToken;
	
	private final static int MAX_FILE_SIZE = 4_096;

	public ClientChatHack(String ip, int port, String path, String login, String password, boolean withPassword)
			throws IOException {
		this.ip = ip;
		this.port = port;
		this.path = path;
		this.login = login;
		this.password = password;
		this.withPassword = withPassword;

		this.sc = SocketChannel.open();
		this.selector = Selector.open();
	}

	private void init() throws IOException {
		socketAddress = new InetSocketAddress(ip, port);
		sc.configureBlocking(false);
		sc.connect(socketAddress);
		serverKey = sc.register(selector, SelectionKey.OP_CONNECT);

		// pour n'envoyer qu'au serveur
		serverKey.attach(new ServerContext(this, serverKey));
		System.out.println("Connection to: " + socketAddress.toString());
	}

	private void launch() throws IOException {
		synchronized (monitor) {

			mainThread = new Thread(() -> {
				try {
					while (!Thread.interrupted()) {
						try {
							selector.select(this::treatKey);
						} catch (UncheckedIOException tunneled) {
							throw tunneled.getCause();
						}
					}
				} catch (IOException e) {
					return;
				}
			});
			mainThread.start();
		}
	}

	private void treatKey(SelectionKey key) {
		try {
			if (key.isValid() && key.isAcceptable()) {
				System.out.println("Someone joins your private channel");
				doAccept(key);
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

		try {
			Context ctx = (Context) key.attachment();
			if (ctx == null) {
				return;
			}
			ctx.processOut();
			ctx.updateInterestOps();

			if (key.isValid() && key.isConnectable()) {
				System.out.println("You are connected to a channel");
				((Context) key.attachment()).doConnect();
			}

			if (key.isValid() && key.isWritable()) {
				((Context) key.attachment()).doWrite();
			}

			if (key.isValid() && key.isReadable()) {
				((Context) key.attachment()).doRead();
			}
		} catch (IOException e) {
			logger.info("Connection closed with client due to IOException");
			silentlyClose(key);
		}
	}

	private void doAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ssc.accept();

		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			clientKey.attach(new PrivateClientContext(this, clientKey));
			
			addPrivateClient(lastDst, lastToken, clientKey);
		}
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();

		try {
			sc.close();
		} catch (IOException e) {
			return;
		}
	}

	public boolean withPassword() {
		return withPassword;
	}

	public void disconnect(SelectionKey key) {
		cnxThread.interrupt();
		readThread.interrupt();
		mainThread.interrupt();
	}

	private void connectToServer() {
		cnxThread = new Thread(() -> {
			ServerContext ctx = (ServerContext) serverKey.attachment();
			if (ctx == null) {
				return;
			}
			ByteBuffer loginBuff = StandardCharsets.UTF_8.encode(login);
			ByteBuffer bb;
			if (withPassword) {
				ByteBuffer pwdBuff = StandardCharsets.UTF_8.encode(password);
				bb = ByteBuffer
						.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + loginBuff.remaining() + pwdBuff.remaining());

				bb.put((byte) 2);
				bb.put((byte) 0);
				bb.putInt(loginBuff.remaining());
				bb.put(loginBuff);
				bb.putInt(pwdBuff.remaining());
				bb.put(pwdBuff);
				bb.flip();

			} else {
				bb = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + loginBuff.remaining());

				bb.put((byte) 2);
				bb.put((byte) 1);
				bb.putInt(loginBuff.remaining());
				bb.put(loginBuff);
				bb.flip();

			}
			ctx.queueFrame(bb);
			selector.wakeup();
		});
		cnxThread.start();
	}

	private void sendFrameToServer() {
		synchronized (monitor) {

			readThread = new Thread(() -> {
				while (!Thread.interrupted()) {
					try (Scanner scan = new Scanner(System.in)) {
						String line;

						while (scan.hasNextLine()) {
							// il faut gerer tous les paquets possibles venant du client

							if (cnxRequest && !sources.isEmpty()) {
								// reponse a la demande de cnx privee
								privateCnxRes(scan);

							} else {
								line = scan.nextLine();

								// msg global
								if (line.startsWith("/ ") || line.startsWith("@ ")) {
									sendGlobalMsg(line.substring(2));
								}

								// msg prive
								else if (line.startsWith("@")) {
									String[] tokens = line.split(" ", 3);
									String dst = tokens[0].substring(1);
									sendPrivateMsg(dst, tokens[1], tokens[2]);
								}

								// logout
								else if (line.equals("logout")) {
									sendLogout();
									selector.wakeup();
									return;
								}
							}
							selector.wakeup();
						}
					} catch (InterruptedException e) {
						return;
					} catch (IOException e) {
						return;
					}
				}
			});
			readThread.start();
		}
	}

	private void privateCnxRes(Scanner scan) throws IOException {
		Random random = new Random();
		String res;

		res = scan.nextLine();
		if (res.equals("0")) {
			acceptPrivateCnx(scan, random);
		} else {
			declinePrivateCnx();
		}
		cnxRequest = false;
	}

	private void acceptPrivateCnx(Scanner scan, Random random) throws IOException {
		ServerContext ctx = (ServerContext) serverKey.attachment();
		if (ctx == null) {
			return;
		}

		// accepter la demande de connexion privee
		// l'adresse ip sera une string mtn
		if (portForPrivate == -1) {
			System.out.println("on which port?");
			portForPrivate = scan.nextInt();
		}

		long token = random.nextLong();
		String address = sc.getLocalAddress().toString();
		String ipString = address.split("/")[1].split(":")[0];
		String dst = sources.poll();
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(login);
		ByteBuffer ipBuff = StandardCharsets.UTF_8.encode(ipString);

		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + 4 * Integer.BYTES + Long.BYTES + srcBuff.remaining()
				+ dstBuff.remaining() + ipBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.putInt(portForPrivate);
		buff.putLong(token);
		buff.putInt(ipBuff.remaining());
		buff.put(ipBuff);
		buff.flip();

		// ouvrir un channel de discussion prive
		if (ssc == null) {
			openPrivateChannel(ipString, token);
		}
		ctx.queueFrame(buff);
		lastDst = dst;
		lastToken = token;
	}

	private void declinePrivateCnx() {
		ServerContext ctx = (ServerContext) serverKey.attachment();
		if (ctx == null) {
			return;
		}

		// refuser la demande de connexion privee
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);
		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();

		ctx.queueFrame(buff);
	}

	private void sendGlobalMsg(String msg) throws InterruptedException {
		ServerContext ctx = (ServerContext) serverKey.attachment();
		if (ctx == null) {
			return;
		}

		ByteBuffer loginBuff = StandardCharsets.UTF_8.encode(login);
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer
				.allocate(Byte.BYTES + 2 * Integer.BYTES + loginBuff.remaining() + msgBuff.remaining());

		buff.put((byte) 3);
		buff.putInt(loginBuff.remaining());
		buff.put(loginBuff);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

		ctx.queueFrame(buff);
	}

	private void sendPrivateMsg(String dst, String type, String msg) throws InterruptedException {
		if (!clients.containsKey(dst)) {
			sendPrivateCnx(dst);
		} else {
			sendPrivateMsgToDst(dst, type, msg);
		}
	}

	private void sendPrivateCnx(String dst) {
		// envoyer une demande de connexion privee au serveur
		ServerContext ctx = (ServerContext) serverKey.attachment();
		if (ctx == null) {
			return;
		}

		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(login);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer buff = ByteBuffer
				.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();

		ctx.queueFrame(buff);
	}

	private void sendPrivateMsgToDst(String dst, String type, String msg) {
		try {
			if (type.equals("0")) {
				sendPrivateTxtToDst(dst, msg);
			} else {
				sendPrivateFileToDst(dst, msg);
			}
		} catch (IOException e) {
			return;
		}
	}

	private void sendPrivateTxtToDst(String dst, String msg) {
		// envoyer le msg directement au client dst
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(login);
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + msgBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 2);
		buff.put((byte) 0);
		buff.putLong(clients.get(dst).getToken());
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

		// envoyer le paquet au bon dst
		Client clientDst = clients.get(dst);
		PrivateClientContext ctx = (PrivateClientContext) clientDst.getKey().attachment();
		if (ctx == null) {
			return;
		}
		
		if (clientDst.getKey().isValid()) {
			ctx.queueFrame(buff);
		} else {
			clients.remove(dst);
		}
	}

	private void sendPrivateFileToDst(String dst, String msg) throws IOException {
		// envoie de fichier au client dst
		// envoie du nom du fichier plus les bytes du fichier
		Path path = Paths.get(this.path + "/" + msg);
		try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
			if (fc.size() > MAX_FILE_SIZE) {
				System.out.println("the file is too big, cannot send it");
				return;
			}
			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(login);
			ByteBuffer fileNameBuff = StandardCharsets.UTF_8.encode(msg);
			ByteBuffer msgBuff = ByteBuffer.allocate(MAX_FILE_SIZE);
			while (fc.read(msgBuff) != -1 && msgBuff.hasRemaining()) {
				;
			}
			msgBuff.flip();
			ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Long.BYTES + 3 * Integer.BYTES + srcBuff.remaining()
					+ fileNameBuff.remaining() + msgBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 2);
			buff.put((byte) 1);
			buff.putLong(clients.get(dst).getToken());
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(fileNameBuff.remaining());
			buff.put(fileNameBuff);
			buff.putInt(msgBuff.remaining());
			buff.put(msgBuff);
			buff.flip();

			Client clientDst = clients.get(dst);
			PrivateClientContext ctx = (PrivateClientContext) clientDst.getKey().attachment();
			if (ctx == null) {
				return;
			}
			
			if (clientDst.getKey().isValid()) {
				ctx.queueFrame(buff);
			} else {
				clients.remove(dst);
			}
		}
	}

	public void sendLogout() throws InterruptedException {
		ServerContext ctx = (ServerContext) serverKey.attachment();
		if (ctx == null) {
			return;
		}
		ByteBuffer buff = ByteBuffer.allocate( Byte.BYTES);
		buff.put((byte) 5);
		buff.flip();
		ctx.queueFrame(buff);
	}
	
	public void addSrc(String src) {
		synchronized (monitor) {
			cnxRequest = true;
			sources.add(src);
		}
	}

	public void addPrivateClient(String src, long token, SelectionKey key) {
		if (!clients.containsKey(src)) {
			clients.put(src, new Client(token, key));
		}
	}

	public void writeMsg(PrivateMsgFrame frame) throws IOException {
		if (frame.getType() == 1) {
			System.out.println(frame.getSrc() + " send you a file");
			Path path = Paths.get(this.path + "/azdazedazd.txt");
			ByteBuffer fileBuff = StandardCharsets.UTF_8.encode(frame.getMsg());

			try (FileChannel fc = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				fc.write(fileBuff);
			}
		} else {
			System.out.println(frame);
		}
	}

	public void connectToClient(String dst, String ip, int port, long token) throws IOException {
		lastDst = dst;
		lastToken = token;

		SocketAddress sa = new InetSocketAddress(ip, port);
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(sa);
		SelectionKey clientKey = sc.register(selector, SelectionKey.OP_CONNECT);

		clients.put(lastDst, new Client(lastToken, clientKey));
		clientKey.attach(new PrivateClientContext(this, clientKey));
		System.out.println("Connected to: " + dst + " /" + ip + ":" + port);

	}

	private void openPrivateChannel(String ip, long token) throws IOException {
		ssc = ServerSocketChannel.open();
		ssc.bind(new InetSocketAddress(ip, portForPrivate));
		ssc.configureBlocking(false);
		ssc.register(selector, SelectionKey.OP_ACCEPT);

		System.out.println("channel: /" + ip + ":" + portForPrivate + " open");
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 4 && args.length != 5) {
			usage();
			return;
		}

		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		String path = args[2];
		String login = args[3];
		String password = "";
		boolean withPassword = false;

		if (args.length == 5) {
			password = args[4];
			withPassword = true;
		}

		ClientChatHack client = new ClientChatHack(ip, port, path, login, password, withPassword);
		client.init();
		client.launch();
		client.connectToServer();
		client.sendFrameToServer();
	}

	private static void usage() {
		logger.info("Usage : ip port path login password");
	}
}
