package chatHack.visitor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import chatHack.frame.GlobalMsgFrame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogOutFrame;
import chatHack.frame.LogResFromServerMDPFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToServerFrame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.SimpleMsgFrame;
import chatHack.server.ServerChatHack;

public class ChatHackServerVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ServerChatHack server;

	public ChatHackServerVisitor(SelectionKey key, ServerChatHack server) {
		this.key = key;
		this.server = server;
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

		// a envoyer a tout le monde
		server.broadcast(key, buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println("log no pwd to mdp");
		Random random = new Random();
		long id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(frame.getName());
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + nameBuff.remaining());

		buff.put((byte) 2);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.flip();

		server.addClient(key, frame.getName());
		server.addClient2(key, id);

		// a envoyer au serveur MDP
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

		// a envoyer au client qui se deconnecte
		server.kickClient(key, buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println("log with pwd to mdp");
		Random random = new Random();
		long id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(frame.getName());
		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(frame.getPassword());
		ByteBuffer buff = ByteBuffer.allocate(
				Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());

		buff.put((byte) 1);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.putInt(passwordBuff.remaining());
		buff.put(passwordBuff);
		buff.flip();

		server.addClient(key, frame.getName());
		server.addClient2(key, id);

		// a envoyer au serveur MDP
		server.sendToMDP(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		System.out.println("private msg cnx accepted to client");

		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(frame.getSrc());
		ByteBuffer ipBuff = StandardCharsets.UTF_8.encode(frame.getIp());
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 3 * Integer.BYTES + Long.BYTES + srcBuff.remaining() + ipBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(frame.getPort());
		buff.putLong(frame.getToken());
		// buff.put(frame.getIpVersion());
		buff.putInt(ipBuff.remaining());
		buff.put(ipBuff);
		buff.flip();

		// a envoyer au client qui a envoye la frame
		server.sendToDst(frame.getSrc(), buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println("private msg cnx refused to cliend");

		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(frame.getSrc());
		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(frame.getErrMsg());
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + errMsgBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(errMsgBuff.remaining());
		buff.put(errMsgBuff);
		buff.flip();

		// a envoyer au client qui a envoye la frame
		server.sendToDst(frame.getSrc(), buff);
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

		// a envoyer au serveur
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		System.out.println("private msg cnx to dst");

		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(frame.getSrc());
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(frame.getDst());
		ByteBuffer buff = ByteBuffer
				.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();

		// a envoyer au destinataire
		server.sendToDst(frame.getDst(), buff);
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

		// a envoyer au client
		return buff;
	}

	@Override
	public ByteBuffer visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
		System.out.println("server mdp res");

		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);

		buff.put(frame.getOpcode());
		buff.putLong(frame.getId());
		buff.flip();

		// a envoyer au client
		server.sendToClient(frame.getId(), buff);
		return buff;
	}
}
