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
import chatHack.visitor.PublicClientNotAuthVisitor;
import chatHack.visitor.FrameVisitor;
import chatHack.visitor.PublicClientAuthVisitor;

public class PublicClientContext implements Context {

	private static int BUFFER_SIZE = 4_096;

	private final SelectionKey key;
	private final SocketChannel sc;
	private final ServerChatHack server;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	private final Queue<ByteBuffer> queue = new LinkedList<>();
	private final Reader<Frame> reader = new FrameToServerReader(bbin);
	private FrameVisitor visitor;
	
	private boolean authenticated = false;
	
	public PublicClientContext(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.server = server;
		this.visitor = new PublicClientNotAuthVisitor(server, this);
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
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
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

	public void authenticate() {
		authenticated = true;
		updateVisitor();
	}
	
	private void updateVisitor() {
		if (authenticated) {
			visitor = new PublicClientAuthVisitor(key, server);
		} else {
			visitor = new PublicClientNotAuthVisitor(server, this);
		}
	}

}
