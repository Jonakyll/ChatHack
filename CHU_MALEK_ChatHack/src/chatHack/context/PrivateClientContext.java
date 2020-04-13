package chatHack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chatHack.client.ClientChatHack;
import chatHack.frame.Frame;
import chatHack.reader.FrameFromClientReader;
import chatHack.reader.Reader;
import chatHack.visitor.PrivateClientVisitor;
import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author MALEK Akram
 * Objet de l'interface Context permettant de recevoir et d'envoyer
 * des messages prives vers un client connecte en prive avec un autre client.
 */
public class PrivateClientContext implements Context {

	private static int BUFFER_SIZE = 1_024;

	private final SelectionKey key;
	private final SocketChannel sc;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);

	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();

	private final Reader<Frame> reader = new FrameFromClientReader(bbin);
	private final FrameVisitor visitor;

	/**
	 * Cree un objet de type PrivateClientContext.
	 * @param client, le client ClientChatHack auquel se connecte un autre client en prive.
	 * @param key, la SelectionKey associee au client.
	 */
	public PrivateClientContext(ClientChatHack client, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.visitor = new PrivateClientVisitor(key, client);
	}

	/**
	 * Lis les donnees recues par un client.
	 */
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

	/**
	 * Ajoute un ByteBuffer a une liste de ByteBuffer a envoyer.
	 * @param buff, un ByteBuffer contenant des donnees.
	 */
	@Override
	public void queueFrame(ByteBuffer buff) {
		queue.add(buff);
		processOut();
		updateInterestOps();
	}

	/**
	 * Prepare le ByteBuffer des donnees a envoyer a un client.
	 * Modifie la taille du ByteBuffer si la donnee a envoyer est trop volumineuse.
	 */
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

	/**
	 * Met a jour les operations possibles sur les donnees.
	 */
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

	/**
	 * Ferme la SelectionKey liee a l'objet Context.
	 */
	@Override
	public void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	/**
	 * Connecte un client a un autre client en prive.
	 * @throws IOException
	 */
	@Override
	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		updateInterestOps();
	}

	/**
	 * Envoie des donnees vers un client.
	 * @throws IOException
	 */
	@Override
	public void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();

		processOut();
		updateInterestOps();
	}

	/**
	 * Lis les donnees recues par un client.
	 * @throws IOException
	 */
	@Override
	public void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}

		processIn();
		updateInterestOps();
	}

	/**
	 * Renvoie la SelectionKey associee a l'objet Context.
	 * @return la SelectionKey associee a l'objet Context.
	 */
	@Override
	public SelectionKey getKey() {
		return key;
	}

	/**
	 * Ferme le context pour ne plus pouvoir recevoir de donnees venant d'un client.
	 */
	public void close() {
		closed = true;
	}
}
