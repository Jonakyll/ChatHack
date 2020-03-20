package chatHack.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import chatHack.frame.Frame;
import chatHack.reader.FrameToServerReader;
import chatHack.reader.GlobalMsgReader;
import chatHack.reader.LogOutToServerReader;
import chatHack.reader.LogReader;
import chatHack.reader.PrivateMsgCnxResToServerReader;
import chatHack.reader.PrivateMsgCnxReader;
import chatHack.reader.Reader;

public class ServerChatHack {

	static private class Context {

		private final SelectionKey key;
		private final SocketChannel sc;
		private final ServerChatHack server;
		private boolean closed = false;
		private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
		private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

		private final Queue<ByteBuffer> queue = new LinkedList<>();
//		private Reader reader;
		private Reader<Frame> reader = new FrameToServerReader(bbin);

		private Context(ServerChatHack server, SelectionKey key) {
			this.key = key;
			this.sc = (SocketChannel) key.channel();
			this.server = server;
		}

		private void processIn() {

			for (;;) {
				Reader.ProcessStatus status = reader.process();

				switch (status) {
				case DONE: {
					Frame frame = (Frame) reader.get();
//					ByteBuffer frameBuff = frame.toByteBuffer();
//					byte opcode = frameBuff.get();

//					if (opcode == 0 || opcode == 1) {
//						server.sendToMDP(frame);
//					}

//					pour le chat global
//					if (opcode == 2) {
					server.broadcast(frame);
//					}

					reader.reset();
					break;
				}

				case REFILL:
					return;

				case ERROR:
					silentlyClose();
					return;
				}
			}
		}

		private void queueFrame(Frame frame) {
			queue.add(frame.toByteBuffer());
			processOut();
			updateInterestOps();
		}

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
				key.interestOps(ops);
			}
		}

		private void silentlyClose() {
			try {
				sc.close();
			} catch (IOException e) {

			}
		}

		public void doWrite() throws IOException {
			bbout.flip();
			sc.write(bbout);
			bbout.compact();

			processOut();
			updateInterestOps();
		}

		public void doRead() throws IOException {
			if (sc.read(bbin) == -1) {
				closed = true;
			}

			processIn();
			updateInterestOps();
		}

	}

	private static int BUFFER_SIZE = 1_024;
	private static Logger logger = Logger.getLogger(ServerChatHack.class.getName());

	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;

	private final String MDPIp;
	private final int MDPPort;

	public ServerChatHack(int port, String MDPIp, int MDPPort) throws IOException {
//		pour connecter le serveur au serverMDP (pas encore fait)
		this.MDPIp = MDPIp;
		this.MDPPort = MDPPort;

		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
	}

	public void launch() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (!Thread.interrupted()) {
			printKeys();
			System.out.println("Starting select");

			try {
				selector.select(this::treatKey);
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
			System.out.println("Select finished");
		}
	}

	private void treatKey(SelectionKey key) {
		printSelectedKey(key);

		try {
			if (key.isValid() && key.isAcceptable()) {
				doAccept(key);
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

		try {
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
		SocketChannel sc = serverSocketChannel.accept();

		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			clientKey.attach(new Context(this, clientKey));
		}
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();

		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	private void broadcast(Frame frame) {
		for (SelectionKey key : selector.keys()) {

			Context ctx = (Context) key.attachment();

			if (ctx == null) {
				continue;
			}
			ctx.queueFrame(frame);
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 3) {
			usage();
			return;
		}
		int port = Integer.parseInt(args[0]);
		String MDPIp = args[1];
		int MDPPort = Integer.parseInt(args[2]);

		ServerChatHack server = new ServerChatHack(port, MDPIp, MDPPort);
		server.launch();
	}

	private static void usage() {
		System.out.println("Usage : port MDPip MDPport");
	}

	/***
	 * Theses methods are here to help understanding the behavior of the selector
	 ***/

	private String interestOpsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		int interestOps = key.interestOps();
		ArrayList<String> list = new ArrayList<>();
		if ((interestOps & SelectionKey.OP_ACCEPT) != 0)
			list.add("OP_ACCEPT");
		if ((interestOps & SelectionKey.OP_READ) != 0)
			list.add("OP_READ");
		if ((interestOps & SelectionKey.OP_WRITE) != 0)
			list.add("OP_WRITE");
		return String.join("|", list);
	}

	public void printKeys() {
		Set<SelectionKey> selectionKeySet = selector.keys();
		if (selectionKeySet.isEmpty()) {
			System.out.println("The selector contains no key : this should not happen!");
			return;
		}
		System.out.println("The selector contains:");
		for (SelectionKey key : selectionKeySet) {
			SelectableChannel channel = key.channel();
			if (channel instanceof ServerSocketChannel) {
				System.out.println("\tKey for ServerSocketChannel : " + interestOpsToString(key));
			} else {
				SocketChannel sc = (SocketChannel) channel;
				System.out.println("\tKey for Client " + remoteAddressToString(sc) + " : " + interestOpsToString(key));
			}
		}
	}

	private String remoteAddressToString(SocketChannel sc) {
		try {
			return sc.getRemoteAddress().toString();
		} catch (IOException e) {
			return "???";
		}
	}

	public void printSelectedKey(SelectionKey key) {
		SelectableChannel channel = key.channel();
		if (channel instanceof ServerSocketChannel) {
			System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
		} else {
			SocketChannel sc = (SocketChannel) channel;
			System.out.println(
					"\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
		}
	}

	private String possibleActionsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		ArrayList<String> list = new ArrayList<>();
		if (key.isAcceptable())
			list.add("ACCEPT");
		if (key.isReadable())
			list.add("READ");
		if (key.isWritable())
			list.add("WRITE");
		return String.join(" and ", list);
	}
}
