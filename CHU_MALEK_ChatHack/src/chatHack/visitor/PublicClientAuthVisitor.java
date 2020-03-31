package chatHack.visitor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import chatHack.frame.GlobalMsgFrame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogOutFrame;
import chatHack.frame.LogResFromServerMDPFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToServerFrame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.PrivateMsgFrame;
import chatHack.frame.SimpleMsgFrame;
import chatHack.server.ServerChatHack;

public class PublicClientAuthVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ServerChatHack server;

	public PublicClientAuthVisitor(SelectionKey key, ServerChatHack server) {
		this.key = key;
		this.server = server;
	}

	@Override
	public ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame) {
		System.out.println("global");
		ByteBuffer buff = frame.getByteBuffer();
		
		// a envoyer a tout le monde
		server.broadcast(key, buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogOutFrame(LogOutFrame frame) {
		System.out.println("log out");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui se deconnecte
		server.removeClient(key, buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		System.out.println("private msg cnx accepted to client");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui a envoye la frame
		server.sendToClientString(frame.getSrc(), buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println("private msg cnx refused to cliend");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui a envoye la frame
		server.sendToClientString(frame.getSrc(), buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		System.out.println("private msg cnx refused to server");
		ByteBuffer buff = frame.getByteBuffer();
		
		// a envoyer au serveur
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		System.out.println("private msg cnx to dst");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au destinataire
		server.sendToClientString(frame.getDst(), buff);
		return buff;
	}

	@Override
	public ByteBuffer visitSimpleMsgFrame(SimpleMsgFrame frame) {
		System.out.println("simple msg");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client
		return buff;
	}

	@Override
	public ByteBuffer visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsg(PrivateMsgFrame frame) {
		return null;
	}

}
