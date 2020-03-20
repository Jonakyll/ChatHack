package chatHack.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import chatHack.frame.Frame;
import chatHack.reader.GlobalMsgReader;
import chatHack.reader.LogOutToClientReader;
import chatHack.reader.PrivateMsgCnxReader;
import chatHack.reader.PrivateMsgCnxResToClientReader;
import chatHack.reader.Reader;
import chatHack.reader.SimpleMsgReader;

public class ClientChatHack {

	private static int BUFFER_SIZE = 1_024;
	private static Logger logger = Logger.getLogger(ClientChatHack.class.getName());

	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final SocketChannel sc;
	private final SocketAddress socketAddress;
	private final Selector selector;
	private SelectionKey uniqueKey;
	private boolean closed = false;

	private Thread readThread;

	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
	private Reader<Frame> reader;
	// private final Reader<Frame> reader = new LogReader(bbin);

	public ClientChatHack(SocketAddress socketAddress) throws IOException {
		this.sc = SocketChannel.open();
		this.socketAddress = socketAddress;
		this.selector = Selector.open();
	}

	public void launch() throws IOException {
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
		checkOpcode();

		for (;;) {
			Reader.ProcessStatus status = reader.process();

			switch (status) {
			case DONE:
				Frame frame = (Frame) reader.get();

				// queueFrame(frame);
				System.out.println(frame);

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

	private void checkOpcode() {
		bbin.flip();

		if (bbin.remaining() >= Byte.BYTES) {

			byte opcode = bbin.get();
			System.out.println("opcode " + opcode);

			switch (opcode) {
			case 0:
				reader = new SimpleMsgReader(bbin);
				break;

			case 1:
				reader = new GlobalMsgReader(bbin);
				break;

			case 2:
				checkStep();
				break;

			case 3:
				reader = new LogOutToClientReader(bbin);
				break;

			case 4:
				reader = new SimpleMsgReader(bbin);
				break;

			default:
				// envoyer un message d'erreur a l'expediteur?
				break;
			}
		}
		bbin.compact();
	}

	private void checkStep() {
		if (bbin.remaining() >= Byte.BYTES) {

			byte step = bbin.get();
			System.out.println("step " + step);

			switch (step) {
			case 0:
				reader = new PrivateMsgCnxReader(bbin);
				break;

			case 1:
				reader = new PrivateMsgCnxResToClientReader(bbin);
				break;

			case 2:
				// reader pour les envoies de msg prives
				break;

			default:
				// envoyer un message d'erreur a l'expediteur?
				break;
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

	public void sendFrame() {
		readThread = new Thread(() -> {

			while (!Thread.interrupted()) {

				try (Scanner scan = new Scanner(System.in)) {
					String line;

					while (scan.hasNextLine()) {
						// il faut gerer tous les paquets possibles venant du client

						line = scan.nextLine();

						ByteBuffer bb = StandardCharsets.UTF_8.encode(line);
						ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + 2 * bb.remaining());

						buff.put((byte) 0);
						buff.put((byte) 0);
						buff.putInt(bb.remaining());
						buff.put(bb);
						buff.putInt(bb.remaining());
						buff.put(bb);

						buff.flip();

						queue.put(buff);
						selector.wakeup();
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		readThread.start();
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			usage();
			return;
		}
		SocketAddress socketAddress = new InetSocketAddress(args[0], Integer.parseInt(args[1]));

		ClientChatHack client = new ClientChatHack(socketAddress);
		client.sendFrame();
		client.launch();
	}

	private static void usage() {
		logger.info("Usage : socketAddress port");
	}
}
