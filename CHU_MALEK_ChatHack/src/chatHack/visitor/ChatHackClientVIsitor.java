package chatHack.visitor;

import java.nio.ByteBuffer;

import chatHack.client.ClientChatHack;
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

public class ChatHackClientVIsitor implements FrameVisitor {

	private final ClientChatHack client;

	public ChatHackClientVIsitor(ClientChatHack client) {
		this.client = client;
	}

	@Override
	public ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitLogOutFrame(LogOutFrame frame) {
		System.out.println(frame);
		client.disconnect();
		return null;
	}

	@Override
	public ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		try {
			System.out.println(frame);
			client.sendPrivateCnxRes(frame.getSrc());
		} catch (InterruptedException e) {
			return null;
		}
		return null;
	}

	@Override
	public ByteBuffer visitSimpleMsgFrame(SimpleMsgFrame frame) {
		System.out.println(frame);
		return null;
	}

	@Override
	public ByteBuffer visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
		if (client.withPassword() && frame.getOpcode() == 1) {
			System.out.println("CONNECTED");
			client.connect();
		}
		if (!client.withPassword() && frame.getOpcode() == 0) {
			System.out.println("CONNECTED");
			client.connect();
		}
		return null;
	}

}
