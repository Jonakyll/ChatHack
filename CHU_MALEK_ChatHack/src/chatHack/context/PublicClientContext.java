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

/**
 * 
 * @author MALEK Akram
 * Objet de l'interface Context permettant de recevoir et d'envoyer
 * des donnees vers un client connecte au serveur ChatHack.
 */
public class PublicClientContext implements Context {

	private static int BUFFER_SIZE = 1_024;

	private final SelectionKey key;
	private final SocketChannel sc;
	private final ServerChatHack server;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	private final Queue<ByteBuffer> queue = new LinkedList<>();
	private final Reader<Frame> reader = new FrameToServerReader(bbin);
	private FrameVisitor visitor;
	
	private boolean authenticated = false;
	
	/**
	 * Cree un objet de type PublicClientContext.
	 * @param server, le serveur ServerChatHack auquel se connecte notre client.
	 * @param key, la SelectionKey associee au client;
	 */
	public PublicClientContext(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.server = server;
		this.visitor = new PublicClientNotAuthVisitor(server, this);
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
	 * Connecte un client au serveur ChatHack.
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

	/**
	 * Change l'etat du client afin qu'il soit authentifie par le serveur.
	 * Il pourra donc recevoir et envoyer d'autres messages.
	 */
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
