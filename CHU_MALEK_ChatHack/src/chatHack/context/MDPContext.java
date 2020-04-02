package chatHack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import chatHack.frame.Frame;
import chatHack.reader.FrameToServerReader;
import chatHack.reader.Reader;
import chatHack.server.ServerChatHack;
import chatHack.visitor.FrameVisitor;
import chatHack.visitor.MDPVisitor;

public class MDPContext implements Context {
	
	private final static int BUFFER_SIZE = 1_024;
	
	private final SelectionKey key;
	private final SocketChannel sc;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	
	private final Queue<ByteBuffer> queue = new LinkedList<>();
	private final Reader<Frame> reader = new FrameToServerReader(bbin);
	private FrameVisitor visitor;

	public MDPContext(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.visitor = new MDPVisitor(server);
	}
	
	@Override
	public void processIn() {
		for (;;) {
			Reader.ProcessStatus status = reader.process();

			switch (status) {
			case DONE:
				Frame frame = (Frame) reader.get();
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
	
	@Override
	public void queueFrame(ByteBuffer buff) {
		queue.add(buff);
		processOut();
		updateInterestOps();
	}

	@Override
	public void processOut() {
		while (!queue.isEmpty()) {
			if (queue.peek().remaining() > bbout.remaining()) {
				ByteBuffer tmp = ByteBuffer.allocate(queue.peek().capacity() + bbout.capacity());
				bbout.flip();
				tmp.put(bbout);
				tmp.put(queue.peek());
				bbout = tmp;
			}
			bbout.put(queue.poll());
		}
	}

	@Override
	public void updateInterestOps() {
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

	@Override
	public void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	@Override
	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		updateInterestOps();
	}

	@Override
	public void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();

		processOut();
		updateInterestOps();
	}

	@Override
	public void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}

		processIn();
		updateInterestOps();
	}
	
	@Override
	public SelectionKey getKey() {
		return key;
	}
	
}
