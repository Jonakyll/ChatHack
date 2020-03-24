package chatHack.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import chatHack.frame.Frame;
import chatHack.reader.FrameToServerReader;
import chatHack.reader.Reader;
import chatHack.visitor.ChatHackServerVisitor;
import chatHack.visitor.FrameVisitor;

public class ServerContext {

	private static int BUFFER_SIZE = 1_024;

	private final SelectionKey key;
	private final SocketChannel sc;
//	private final ServerChatHack server;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	private final Queue<ByteBuffer> queue = new LinkedList<>();
//	private Reader reader;
	private final Reader<Frame> reader = new FrameToServerReader(bbin);
	
	private final FrameVisitor visitor;
	
	public ServerContext(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
//		this.server = server;
		this.visitor = new ChatHackServerVisitor(key, server);
	}
	
	public SelectionKey getKey() {
		return key;
	}

	private void processIn() {

		for (;;) {
			Reader.ProcessStatus status = reader.process();

			switch (status) {
			case DONE: {
				Frame frame = (Frame) reader.get();
				frame.accept(visitor);
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

	public void queueFrame(ByteBuffer buff) {
		queue.add(buff);
		processOut();
		updateInterestOps();
	}

	private void processOut() {
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
			bbout.put(queue.poll());
		}
	}

	private void updateInterestOps() {
		System.out.println("bbin " + bbin.remaining());
		System.out.println("bbout " + bbout.remaining());
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

	public void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {

		}
	}
	
	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		updateInterestOps();
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

	public void close() {
		closed = true;
	}

}
