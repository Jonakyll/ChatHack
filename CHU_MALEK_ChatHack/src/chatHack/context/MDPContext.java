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

/**
 * 
 * @author MALEK Akram
 * Objet de l'interface Context permettant de recevoir et d'envoyer
 * des donnees vers le serveur MDP pour etablir ou non la connexion d'un client.
 */
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

	/**
	 * Cree un objet de type MDPContext.
	 * @param server, le serveur ChatHack qui se connecte a un serveur MDP.
	 * @param key, la SelectionKey associee au serveur MDP.
	 */
	public MDPContext(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.visitor = new MDPVisitor(server);
	}
	
	/**
	 * Lis les donnees recues par le serveur MDP.
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
	 * Prepare le ByteBuffer des donnees a envoyer au serveur MDP.
	 */
	@Override
	public void processOut() {
		while (!queue.isEmpty() && queue.peek().remaining() < bbout.remaining()) {
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
	 * Connecte le serveur ChatHach a un serveur MDP.
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
	 * Envoie des donnees vers le serveur MDP.
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
	 * Lis les donnees recues par le serveur MDP.
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
	
}
