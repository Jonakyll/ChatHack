package chatHack.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import chatHack.frame.Frame;
import chatHack.reader.FrameToClientReader;
import chatHack.reader.Reader;
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
				System.out.println(socketAddress.toString());

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
		readThread = new Thread(() -> {

			while (!Thread.interrupted()) {
				// if (!connected) {
				// mainThread.interrupt();
				// return;
				// }
				System.out.println("you are connected");
				try (Scanner scan = new Scanner(System.in)) {
					String line;

					while (scan.hasNextLine() == !closed) {
						// il faut gerer tous les paquets possibles venant du client

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
						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		readThread.start();
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
			
			ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
			buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + dstBuff.remaining());
			
			buff.put((byte) 4);
			buff.put((byte) 0);
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
	
	public void sendPrivateCnxRes() {
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("0     = accept");
			System.out.println("other = decline");
			byte res;
			ByteBuffer buff;
			
			res = scan.nextByte();
			
			if (res == 0) {
//				accepter la demande de connexion privee
				buff = ByteBuffer.allocate(4 * Byte.BYTES + Integer.BYTES + Long.BYTES);
				
			} else {
//				refuser la demande de connexion privee
				
			}
		}
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
