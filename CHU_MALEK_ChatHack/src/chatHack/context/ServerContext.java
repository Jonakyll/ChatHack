package chatHack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chatHack.client.ClientChatHack;
import chatHack.frame.Frame;
import chatHack.reader.FrameToClientReader;
import chatHack.reader.Reader;
import chatHack.visitor.ServerVisitor;
import chatHack.visitor.FrameVisitor;

public class ServerContext implements Context {
	
	private static int BUFFER_SIZE = 1_024;
	
	private final SelectionKey key;
	private final SocketChannel sc;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();

	private final Reader<Frame> reader = new FrameToClientReader(bbin);
	private final FrameVisitor visitor;
	
	public ServerContext(ClientChatHack client, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.visitor = new ServerVisitor(key, client);
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
	
	public void close() {
		closed = true;
	}
	
}
