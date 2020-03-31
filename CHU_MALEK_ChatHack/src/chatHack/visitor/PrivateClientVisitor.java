package chatHack.visitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

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
import chatHack.frame.PrivateMsgFrame;
import chatHack.frame.SimpleMsgFrame;

public class PrivateClientVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ClientChatHack client;

	public PrivateClientVisitor(SelectionKey key, ClientChatHack client) {
		this.key = key;
		this.client = client;
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
		return null;
	}

	@Override
	public ByteBuffer visitPrivateMsg(PrivateMsgFrame frame) {
		try {
			// ajouter la nouvelle src
			client.addPrivateClient(frame.getSrc(), frame.getToken(), key);
			client.writeMsg(frame);
		} catch (IOException e) {
			return null;
		}
		return null;
	}

}
