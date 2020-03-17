package chatHack.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

public class ClientChatHack {

	private static int BUFFER_SIZE = 1_024;
	private static Logger logger = Logger.getLogger(ClientChatHack.class.getName());

	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	private final SocketChannel sc;
	private final SocketAddress socketAddress;
	private final Selector selector;
	private SelectionKey uniqueKey;
	private boolean closed = false;

	private Thread clientThread;
	private Thread readThread;

	public ClientChatHack(SocketAddress socketAddress) throws IOException {
		this.sc = SocketChannel.open();
		this.socketAddress = socketAddress;
		this.selector = Selector.open();
	}

	public void launch() throws IOException {
		clientThread = new Thread(() -> {

			try {
				sc.configureBlocking(false);
				sc.connect(socketAddress);
				uniqueKey = sc.register(selector, SelectionKey.OP_CONNECT);

				System.out.println("Connected to: " + socketAddress.toString());
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
		clientThread.start();
	}

	private void treatKey(SelectionKey key) {

		try {

			if (key.isValid() && key.isConnectable()) {
				doConnect();
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

		try {

			if (key.isValid() && key.isWritable()) {
				doWrite();
			}

			if (key.isValid() && key.isReadable()) {
				doRead();
			}
		} catch (IOException e) {
			logger.info("Connection closed with client due to IOException");
			silentlyClose();
		}
	}

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
		bbin.flip();

		while (bbin.hasRemaining()) {
			System.out.println(bbin.get());
		}

		bbin.compact();
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
						line = scan.nextLine();
						ByteBuffer bb = StandardCharsets.UTF_8.encode(line);

						while (bbout.remaining() >= Byte.BYTES && bb.remaining() >= Byte.BYTES) {
							bbout.put(bb.get());
							selector.wakeup();
						}

					}
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
		System.out.println("Usage : socketAddress port");
	}
}
