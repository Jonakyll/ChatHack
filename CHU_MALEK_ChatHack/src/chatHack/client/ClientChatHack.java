package chatHack.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import chatHack.frame.Frame;
import chatHack.reader.FrameToClientReader;
import chatHack.reader.Reader;
import chatHack.server.Context;
import chatHack.visitor.ChatHackClientVIsitor;
import chatHack.visitor.FrameVisitor;

public class ClientChatHack {

	private static int BUFFER_SIZE = 1_024;
	private static Logger logger = Logger.getLogger(ClientChatHack.class.getName());

	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final SocketChannel sc;
	private SocketAddress socketAddress;
	private final Selector selector;
	private SelectionKey uniqueKey;
	private boolean closed = false;

	private Thread mainThread;
	private Thread readThread;
	private Thread cnxThread;

	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
	// private Reader<Frame> reader;
	private final Reader<Frame> reader = new FrameToClientReader(bbin);

	private final String ip;
	private final int port;
	private final String path;
	private final String login;
	private final String password;
	private final boolean withPassword;
	private boolean connected = false;

	private final FrameVisitor visitor = new ChatHackClientVIsitor(this);

	// private final Map<String, Long> clients = new HashMap<>();
	private final SynchronizedPrivateClients clients = new SynchronizedPrivateClients();

	private final Map<ServerSocketChannel, Long> channels = new HashMap<>();

	private final BlockingQueue<String> sources = new LinkedBlockingQueue<>();
	private boolean cnxRequest = false;

	private final Object monitor = new Object();

	public ClientChatHack(String ip, int port, String path, String login, String password, boolean withPassword)
			throws IOException {
		this.ip = ip;
		this.port = port;
		this.path = path;
		this.login = login;
		this.password = password;
		this.withPassword = withPassword;

		this.sc = SocketChannel.open();
		// this.socketAddress = socketAddress;
		this.selector = Selector.open();
	}

	private void launch() throws IOException {
		mainThread = new Thread(() -> {

			try {
				socketAddress = new InetSocketAddress(ip, port);
				System.out.println("SOCKETADDRESS: " + socketAddress.toString());

				sc.configureBlocking(false);
				sc.connect(socketAddress);
				uniqueKey = sc.register(selector, SelectionKey.OP_CONNECT);

				System.out.println("Connected to: " + socketAddress.toString());
				Set<SelectionKey> selectedKeys = selector.selectedKeys();

				while (!Thread.interrupted()) {

					// try {
					// processOut();
					// updateInterestOps();
					// selector.select(this::treatKey);
					//
					//
					// } catch (UncheckedIOException tunneled) {
					// throw tunneled.getCause();
					// }

					selector.select();
					processOut();
					updateInterestOps();
					processSelectedKeys(selectedKeys);
					selectedKeys.clear();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		mainThread.start();
	}

	private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
		for (SelectionKey key : selectedKeys) {
			// accepter la cnx d'un client en prive
			// if (key.isValid() && key.isAcceptable()) {
			// doAccept(key);
			// }

			if (key.isValid() && key.isConnectable()) {
				doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				doRead();
			}
		}
	}

	// private void treatKey(SelectionKey key) {
	//
	// try {
	//
	// if (key.isValid() && key.isConnectable()) {
	// doConnect();
	// }
	// } catch (IOException ioe) {
	// throw new UncheckedIOException(ioe);
	// }
	//
	// try {
	//
	// if (key.isValid() && key.isWritable()) {
	// doWrite();
	// }
	//
	// if (key.isValid() && key.isReadable()) {
	// System.out.println("wesh");
	// doRead();
	// }
	// } catch (IOException e) {
	// logger.info("Connection closed with client due to IOException");
	// silentlyClose();
	// }
	// }

	private void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		updateInterestOps();
	}

	private void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();

		processOut();
		updateInterestOps();
	}

	private void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}

		processIn();
		updateInterestOps();
	}

	private void processIn() {

		for (;;) {
			Reader.ProcessStatus status = reader.process();

			switch (status) {
			case DONE:
				Frame frame = (Frame) reader.get();
				// queueFrame(frame);
				frame.accept(visitor);

				reader.reset();
				break;

			case REFILL:
				return;

			case ERROR:
				silentlyClose();
				return;
			}
		}
	}

	// private void queueFrame(Frame frame) {
	// queue.add(frame.toByteBuffer());
	// processOut();
	// updateInterestOps();
	// }

	private void processOut() {
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
			bbout.put(queue.poll());
		}
	}

	private void updateInterestOps() {
		int ops = 0;

		if (bbin.hasRemaining() && !closed) {
			ops |= SelectionKey.OP_READ;
		}
		if (bbout.position() != 0) {
			ops |= SelectionKey.OP_WRITE;
		}
		if (ops == 0) {
			silentlyClose();
		} else {
			uniqueKey.interestOps(ops);
		}
	}

	private void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	public boolean withPassword() {
		return withPassword;
	}

	public void connect() {
		connected = true;
	}

	public boolean isConnected() {
		return connected;
	}

	public void disconnect() {
		closed = true;
		silentlyClose();
		cnxThread.interrupt();
		readThread.interrupt();
		mainThread.interrupt();
	}

	private void connectToServer() {
		cnxThread = new Thread(() -> {
			System.out.println("try to connect");
			try {
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
				queue.put(bb);
				selector.wakeup();
			} catch (InterruptedException e) {
				return;
			}
		});
		cnxThread.start();
	}

	private void sendFrameToServer() {
		synchronized (monitor) {

			readThread = new Thread(() -> {

				while (!Thread.interrupted()) {
					// if (!connected) {
					// mainThread.interrupt();
					// return;
					// }
					System.out.println("you are connected");
					try (Scanner scan = new Scanner(System.in)) {
						String line;

						while (scan.hasNextLine() && !closed) {
							// il faut gerer tous les paquets possibles venant du client

							if (cnxRequest && !sources.isEmpty()) {
								Random random = new Random();
								String res;
								int port;
								ByteBuffer buff;

								res = scan.nextLine();
								if (res.equals("0")) {
									// accepter la demande de connexion privee
									// l'adresse ip sera une string mtn
									System.out.println("on which port?");

									port = scan.nextInt();
									long token = random.nextLong();
									String address = socketAddress.toString();
									String ipString = address.split("/")[1].split(":")[0];
									ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(sources.poll());
									ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(login);
									ByteBuffer ipBuff = StandardCharsets.UTF_8.encode(ipString);

									buff = ByteBuffer.allocate(3 * Byte.BYTES + 4 * Integer.BYTES + Long.BYTES
											+ srcBuff.remaining() + dstBuff.remaining() + ipBuff.remaining());

									buff.put((byte) 4);
									buff.put((byte) 1);
									buff.put((byte) 0);
									buff.putInt(srcBuff.remaining());
									buff.put(srcBuff);
									buff.putInt(dstBuff.remaining());
									buff.put(dstBuff);
									buff.putInt(port);
									buff.putLong(token);
									buff.putInt(ipBuff.remaining());
									buff.put(ipBuff);

									// ouvrir un channel de discussion prive
									openPrivateChannel(ipString, port, token);

								} else {
									// refuser la demande de connexion privee
									buff = ByteBuffer.allocate(3 * Byte.BYTES);
									buff.put((byte) 4);
									buff.put((byte) 1);
									buff.put((byte) 1);
								}

								buff.flip();
								queue.put(buff);
								selector.wakeup();
								cnxRequest = false;

							} else {

								line = scan.nextLine();

								// msg global
								if (line.startsWith("/ ") || line.startsWith("@ ")) {
									sendGlobalMsg(line.substring(2));
								}

								// msg prive
								if (line.startsWith("@")) {
									String[] tokens = line.split(" ", 2);
									String dst = tokens[0].substring(1);
									sendPrivateMsg(dst, tokens[1]);
								}

								// logout
								if (line.equals("logout")) {
									sendLogout();
									return;
								}
								
							}
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

	private void sendGlobalMsg(String msg) throws InterruptedException {
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

		queue.put(buff);
		selector.wakeup();
	}

	private void sendPrivateMsg(String dst, String msg) throws InterruptedException {
		ByteBuffer buff;
		if (!this.clients.containsKey(dst)) {
			// envoyer une demande de connexion privee au serveur

			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(login);
			ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
			buff = ByteBuffer.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 0);
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(dstBuff.remaining());
			buff.put(dstBuff);
			buff.flip();

			queue.put(buff);
			selector.wakeup();
		} else {
			// envoyer le msg directement au client dst

		}

	}

	private void sendLogout() throws InterruptedException {
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES);

		buff.put((byte) 5);
		buff.put((byte) 0);
		buff.flip();

		queue.put(buff);
		selector.wakeup();
	}

	public void addClient(String src) {
		synchronized (monitor) {
			cnxRequest = true;
			sources.add(src);
		}
	}

	public void connectToClient(String dst, String ip, int port, long token) {
		
	}

	private void openPrivateChannel(String ip, int port, long token) throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.bind(new InetSocketAddress(ip, port));
		ssc.configureBlocking(false);
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		channels.put(ssc, token);
		System.out.println("channel " + ssc + " opened");
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
		client.connectToServer();
		client.sendFrameToServer();
		client.launch();
	}

	private static void usage() {
		logger.info("Usage : ip port path login password");
	}
}
