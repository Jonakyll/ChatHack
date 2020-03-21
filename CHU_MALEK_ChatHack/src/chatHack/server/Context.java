package chatHack.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import chatHack.frame.Frame;
import chatHack.frame.GlobalMsgFrame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogOutFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToServerFrame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.ServerMDPResFrame;
import chatHack.frame.SimpleMsgFrame;
import chatHack.reader.FrameToServerReader;
import chatHack.reader.Reader;
import chatHack.visitor.FrameVisitor;

public class Context implements FrameVisitor {

	private static int BUFFER_SIZE = 1_024;

	private final SelectionKey key;
	private final SocketChannel sc;
	private final ServerChatHack server;
	private boolean closed = false;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	private final Queue<ByteBuffer> queue = new LinkedList<>();
//	private Reader reader;
	private Reader<Frame> reader = new FrameToServerReader(bbin);
	
//	private FrameVisitor visitor;

	public Context(ServerChatHack server, SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.server = server;
//		this.visitor = new ConcreteFrameVisitor(server);
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
				frame.accept(this);
//				ByteBuffer frameBuff = frame.toByteBuffer();
//				byte opcode = frameBuff.get();

//				if (opcode == 0 || opcode == 1) {
//					server.sendToMDP(frame);
//				}

//				pour le chat global
//				if (opcode == 2) {
//				server.broadcast(frame);
//				}

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

	private void silentlyClose() {
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

	@Override
	public ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame) {
		System.out.println("global");
		ByteBuffer expBuff = StandardCharsets.UTF_8.encode(frame.getExp());
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(frame.getMsg());
		ByteBuffer buff = ByteBuffer
				.allocate(Byte.BYTES + 2 * Integer.BYTES + expBuff.remaining() + msgBuff.remaining());

		buff.put((byte) 3);
		buff.putInt(expBuff.remaining());
		buff.put(expBuff);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

//		a envoyer a tout le monde
		server.broadcast(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println("log no pwd to mdp");
		Random random = new Random();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(frame.getName());
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + nameBuff.remaining());

		buff.put((byte) 2);
		buff.putLong(random.nextLong());
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.flip();

//		a envoyer au serveur MDP
		server.sendToMDP(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogOutFrame(LogOutFrame frame) {
		System.out.println("log out");
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(frame.getMsg());
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + msgBuff.remaining());

		buff.put((byte) 5);
		buff.put(frame.getLogoutType());
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

//		a envoyer au client qui se deconnecte
		return buff;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println("log with pwd to mdp");
		Random random = new Random();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(frame.getName());
		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(frame.getPassword());
		ByteBuffer buff = ByteBuffer.allocate(
				Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());

		buff.put((byte) 1);
		buff.putLong(random.nextLong());
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.putInt(passwordBuff.remaining());
		buff.put(passwordBuff);
		buff.flip();

//		a envoyer au serveur MDP
		server.sendToMDP(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		System.out.println("private msg cnx accepted to client");
		ByteBuffer ipBuff = ByteBuffer.allocate(frame.getIpVersion() * Byte.BYTES);

		frame.getIp().forEach(i -> ipBuff.put(i));
		ipBuff.flip();

		ByteBuffer buff = ByteBuffer.allocate(4 * Byte.BYTES + Integer.BYTES + Long.BYTES + ipBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(frame.getPort());
		buff.putLong(frame.getToken());
		buff.put(frame.getIpVersion());
		buff.put(ipBuff);
		buff.flip();

//		a envoyer au client qui a envoye la frame
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println("private msg cnx refused to cliend");
		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(frame.getErrMsg());
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Integer.BYTES + errMsgBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.putInt(errMsgBuff.remaining());
		buff.put(errMsgBuff);
		buff.flip();

//		a envoyer au client qui a envoye la frame
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		System.out.println("private msg cnx refused to server");
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();

//		a envoyer au serveur
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		System.out.println("private msg cnx to dst");
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(frame.getDst());
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + dstBuff.remaining());

		buff.put((byte) 4);
		buff.put(frame.getStep());
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();

//		a envoyer au destinataire
		return buff;
	}

	@Override
	public ByteBuffer visitSimpleMsgFrame(SimpleMsgFrame frame) {
		System.out.println("simple msg");
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(frame.getMsg());
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());

		buff.put(frame.getOpcode());
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

//		a envoyer au client
		return buff;
	}

	@Override
	public ByteBuffer visitServerMDPResFrame(ServerMDPResFrame frame) {
		System.out.println("server mdp res");
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
		
		buff.put(frame.getOpcode());
		buff.putLong(frame.getId());
		buff.flip();
		
//		connecter le client ou non
		
//		test
		server.broadcast(buff);
		return buff;
	}

}
