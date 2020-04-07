package chatHack.visitor;

import java.nio.ByteBuffer;

import chatHack.context.PublicClientContext;
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

public class PublicClientNotAuthVisitor implements FrameVisitor {

	private final ServerChatHack server;
	private final PublicClientContext ctx;

	public PublicClientNotAuthVisitor(ServerChatHack server, PublicClientContext ctx) {
		this.server = server;
		this.ctx = ctx;
	}

	@Override
	public ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println("log no pwd to mdp");
		ByteBuffer buff = frame.getByteBuffer();
		
		server.addClientString(frame.getName(), ctx);
		server.addClientLong(frame.getId(), ctx);

		// a envoyer au serveur MDP + desenregistrer le client context du selector
		server.sendToMDP(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitLogOutFrame(LogOutFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println("log with pwd to mdp");
		ByteBuffer buff = frame.getByteBuffer();
		
		server.addClientString(frame.getName(), ctx);
		server.addClientLong(frame.getId(), ctx);

		// a envoyer au serveur MDP  + desenregistrer le client context du selector
		server.sendToMDP(buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitSimpleMsgFrame(SimpleMsgFrame frame) {
		return null;
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
