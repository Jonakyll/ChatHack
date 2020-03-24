package chatHack.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chatHack.frame.Frame;
import chatHack.reader.FrameToClientReader;
import chatHack.reader.Reader;
import chatHack.visitor.ChatHackClientVIsitor;
import chatHack.visitor.FrameVisitor;

public class ClientContext {
	
	private static int BUFFER_SIZE = 1_024;
	
	private final SelectionKey key;
	private final SocketChannel sc;
	private final ClientChatHack client;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();

	private final Reader<Frame> reader = new FrameToClientReader(bbin);
	private final FrameVisitor visitor;
	
	private boolean connected = false;

	
	public ClientContext(ClientChatHack client, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.client = client;
		this.visitor = new ChatHackClientVIsitor(key, client);
	}
	
	private void processIn() {

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
	
	public void queueFrame(ByteBuffer buff) {
		queue.add(buff);
		processOut();
		updateInterestOps();
	}
	
	public void processOut() {
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
			bbout.put(queue.poll());
		}
	}
	
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
	
	public void connect() {
		connected = true;
	}
}
