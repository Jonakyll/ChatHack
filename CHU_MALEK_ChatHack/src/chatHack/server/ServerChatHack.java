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
import java.util.Set;
import java.util.logging.Logger;

public class ServerChatHack {

	private static Logger logger = Logger.getLogger(ServerChatHack.class.getName());

	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;

	private final String MDPIp;
	private final int MDPPort;
	private SocketAddress socketAdress;
	private final SocketChannel sc;
	private SelectionKey MDPKey;

	private final SynchronizedClients<String> clients = new SynchronizedClients<>();
	private final SynchronizedClients<Long> clients2 = new SynchronizedClients<>();

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
		MDPKey.attach(new ServerContext(this, MDPKey));

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
				((ServerContext) key.attachment()).doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				((ServerContext) key.attachment()).doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				((ServerContext) key.attachment()).doRead();
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
			clientKey.attach(new ServerContext(this, clientKey));
		}
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();

		try {
			sc.close();
		} catch (IOException e) {

		}
	}

	public void broadcast(SelectionKey key, ByteBuffer buff) {
		for (SelectionKey k : this.clients.values()) {

			ServerContext ctx = (ServerContext) k.attachment();

			if (ctx == null) {
				continue;
			}

			if (k != key) {
				ctx.queueFrame(buff.duplicate());
			}
		}
	}

	public void addClient(SelectionKey key, String name) {
		this.clients.put(name, key);
	}

	public void addClient2(SelectionKey key, long id) {
		this.clients2.put(id, key);
	}

	public void sendToMDP(ByteBuffer buff) {
		ServerContext ctx = (ServerContext) MDPKey.attachment();

		if (ctx == null) {
			return;
		}
		ctx.queueFrame(buff);
	}

	public void sendToClient(long id, ByteBuffer buff) {
		SelectionKey key = this.clients2.get(id);

		if (key == null) {
			return;
		}
		ServerContext ctx = (ServerContext) key.attachment();

		if (ctx == null) {
			return;
		}
		ctx.queueFrame(buff);
	}

	public void kickClient(SelectionKey key, ByteBuffer buff) {
		for (String name : clients.keySet()) {
			if (clients.get(name) == key) {
				clients.remove(name);
			}
		}

		for (long id : clients2.keySet()) {
			if (clients2.get(id) == key) {
				clients2.remove(id);
			}
		}

		ServerContext ctx = (ServerContext) key.attachment();

		if (ctx == null) {
			return;
		}
		ctx.queueFrame(buff);
		ctx.close();
	}

	public void sendToDst(String dst, ByteBuffer buff) {
		SelectionKey key = clients.get(dst);

		if (key == null) {
			return;
		}

		ServerContext ctx = (ServerContext) key.attachment();

		if (ctx == null) {
			return;
		}

		ctx.queueFrame(buff);

	}

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

	public void printKeys() {
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

	public void printSelectedKey(SelectionKey key) {
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
