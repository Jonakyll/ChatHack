package chatHack.visitor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import chatHack.context.MDPContext;
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

public class MDPVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ServerChatHack server;
	private final MDPContext ctx;
	
	public MDPVisitor(SelectionKey key, ServerChatHack server, MDPContext ctx) {
		this.key = key;
		this.server = server;
		this.ctx = ctx;
	}
	
	@Override
	public ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogOutFrame(LogOutFrame frame) {
		return null;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		return null;
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
		System.out.println("server mdp res");
		ByteBuffer buff = frame.getByteBuffer();
		
		// a envoyer au client
		server.sendToClientLong(frame.getId(), buff);
		return buff;
	}

	@Override
	public ByteBuffer visitPrivateMsg(PrivateMsgFrame frame) {
		return null;
	}

}
