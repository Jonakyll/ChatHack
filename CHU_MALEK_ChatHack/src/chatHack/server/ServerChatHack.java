package chatHack.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import chatHack.frame.Frame;
import chatHack.reader.Reader;

public class ServerChatHack {

	static private class Context {

		private final SelectionKey key;
		private final SocketChannel sc;
		private final ServerChatHack server;
		private boolean closed = false;
		private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
		private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
		
		private final Queue<Frame> queue = new LinkedList<>();
		private Reader reader;
		
		private Context(ServerChatHack server, SelectionKey key) {
			this.key = key;
			this.sc = (SocketChannel) key.channel();
			this.server = server;
		}

		private void processIn() {
			checkOpcode();
		}
		
		private void checkOpcode() {
			byte opcode = bbin.get();
			
			switch (opcode) {
			case '0': {
//				reader = new 
				return;
			}
			case '1': {
				return;
			}
			case '2': {
				return;
			}
			case '3': {
				return;
			}
			default: {
//				envoyer un message d'erreur a l'expediteur?
				
				return;
			}
			}
		}

		private void queueFrame(Frame frame) {
			queue.add(frame);
			processOut();
			updateInterestOps();
		}

		private void processOut() {
			while (!queue.isEmpty()) {
				bbout.put(queue.poll().toByteBuffer());
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

	public ServerChatHack(int port) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
	}

	public void launch() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (!Thread.interrupted()) {

			try {
				selector.select(this::treatKey);
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	private void treatKey(SelectionKey key) {
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
		if (args.length != 1) {
			usage();
			return;
		}
		new ServerChatHack(Integer.parseInt(args[0])).launch();
	}

	private static void usage() {
		System.out.println("Usage : ServerSumBetter port");
	}
}
