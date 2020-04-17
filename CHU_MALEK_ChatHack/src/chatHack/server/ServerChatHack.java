package chatHack.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import chatHack.context.Context;
import chatHack.context.MDPContext;
import chatHack.context.PublicClientContext;

/**
 * 
 * @author CHU Jonathan
 * Objet representant un serveur pour l'application ChatHack.
 * Il permet d'identifier un client a l'aide d'un autre serveur MDP ou
 * de diffuser des messages textuels.
 */
public class ServerChatHack {

	private static Logger logger = Logger.getLogger(ServerChatHack.class.getName());

	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;

	private final String MDPIp;
	private final int MDPPort;
	private SocketAddress socketAdress;
	private final SocketChannel sc;
	private SelectionKey MDPKey;

	private final Map<String, PublicClientContext> clientsString = new HashMap<>();
	private final Map<Long, PublicClientContext> clientsLong = new HashMap<>();

	/**
	 * Cree un objet de type ServerChatHack.
	 * @param port, le port sur lequel le serveur est accessible.
	 * @param MDPIp, l'adresse ip du serveur MDP afin d'identifier des clients.
	 * @param MDPPort, le port sur lequel le serveur MDP est accessible.
	 * @throws IOException
	 */
	public ServerChatHack(int port, String MDPIp, int MDPPort) throws IOException {
		this.MDPIp = MDPIp;
		this.MDPPort = MDPPort;
		this.sc = SocketChannel.open();

		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
	}

	private void launch() throws IOException {
		socketAdress = new InetSocketAddress(MDPIp, MDPPort);
		sc.configureBlocking(false);
		sc.connect(socketAdress);
		MDPKey = sc.register(selector, SelectionKey.OP_CONNECT);
		MDPKey.attach(new MDPContext(this, MDPKey));

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
			if (key.isValid() && key.isConnectable()) {
				((Context) key.attachment()).doConnect();
			}
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
			clientKey.attach(new PublicClientContext(this, clientKey));
		}
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();

		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	/**
	 * Diffuse un message textuel envoye depuis un client vers
	 * tous les autres clients connectes au serveur ChatHack.
	 * @param key, la SelectionKey du client qui envoie le message.
	 * @param buff, le ByteBuffer contenant le message a diffuser.
	 */
	public void broadcast(SelectionKey key, ByteBuffer buff) {
		for (PublicClientContext ctx : this.clientsString.values()) {
			if (ctx == null) {
				continue;
			}
			if (ctx.getKey() != key && ctx.getKey().isValid()) {
				ctx.queueFrame(buff.duplicate());
			}
		}
	}

	/**
	 * Ajoute un client dans la liste des clients connectes au serveur ChatHack.
	 * @param name, le pseudo du client.
	 * @param ctx, son PublicClientContext associe.
	 */
	public void addClientString(String name, PublicClientContext ctx) {
		this.clientsString.put(name, ctx);
	}

	/**
	 * Ajoute un client dans la liste des clients authentifies par le serveur MDP.
	 * @param id
	 * @param ctx
	 */
	public void addClientLong(long id, PublicClientContext ctx) {
		this.clientsLong.put(id, ctx);
	}

	/**
	 * Envoie une frame d'authentification au serveur MDP pour
	 * connecter (ou non) un client au serveur ChatHack.
	 * @param buff, le ByteBuffer contenant les informations liees au client a identifier.
	 */
	public void sendToMDP(ByteBuffer buff) {
		MDPContext ctx = (MDPContext) MDPKey.attachment();
		if (ctx == null) {
			return;
		}
		if (ctx.getKey().isValid()) {
			ctx.queueFrame(buff);
		}
	}

	/**
	 * Envoie la frame reponse du serveur MDP au client qui demande
	 * la connexion au serveur ChatHack.
	 * @param id, l'identifiant du client qui demande a se connecter.
	 * @param buff, le ByteBuffer contenant la reponse du serveur MDP.
	 */
	public void sendToClientLong(long id, ByteBuffer buff) {
		PublicClientContext ctx = clientsLong.get(id);
		if (ctx == null) {
			return;
		}
		if (ctx.getKey().isValid()) {
			ctx.queueFrame(buff);
			ctx.authenticate();
		}
	}

	/**
	 * Envoie une frame de deconnexion au client qui demande a quitter le serveur ChatHack.
	 * @param key, la SelectionKey associee a client qui veut se deconnecter.
	 * @param buff, le ByteBuffer de deconnexion.
	 */
	public void removeClient(SelectionKey key, ByteBuffer buff) {
		PublicClientContext ctx = (PublicClientContext) key.attachment();
		if (ctx == null) {
			return;
		}
		if (ctx.getKey().isValid()) {
			ctx.queueFrame(buff);
			ctx.close();
		}
	}

	/**
	 * Envoie une frame de connexion privee a un client destinataire.
	 * @param dst, le pseudo du client a qui on demande la connexion privee.
	 * @param buff, le ByteBuffer de demande de connexion privee.
	 */
	public void sendToClientString(String dst, ByteBuffer buff) {
		Context ctx = clientsString.get(dst);
		if (ctx == null) {
			return;
		}
		if (ctx.getKey().isValid()) {
			ctx.queueFrame(buff);
		}
	}
	
//================================================================================

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

	private void printKeys() {
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

	private void printSelectedKey(SelectionKey key) {
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
