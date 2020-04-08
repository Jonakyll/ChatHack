package chatHack.visitor;

import java.io.IOException;
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

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface FrameVisitor qui permet
 * a un client ChatHack d'analyser des donnees recues par un autre client
 * envoye par messages prives.
 */
public class PrivateClientVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ClientChatHack client;

	/**
	 * Cree un objet de type PrivateClientVisitor.
	 * @param key, la SelectionKey associee au client.
	 * @param client, le ClientChatHack associe au client.
	 */
	public PrivateClientVisitor(SelectionKey key, ClientChatHack client) {
		this.key = key;
		this.client = client;
	}

	@Override
	public void visitGlobalMsgFrame(GlobalMsgFrame frame) {
	}

	@Override
	public void visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
	}

	@Override
	public void visitLogOutFrame(LogOutFrame frame) {
	}

	@Override
	public void visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
	}

	@Override
	public void visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
	}

	@Override
	public void visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
	}

	@Override
	public void visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
	}

	@Override
	public void visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
	}

	@Override
	public void visitSimpleMsgFrame(SimpleMsgFrame frame) {
	}

	@Override
	public void visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
	}

	/**
	 * Visite une frame de message prive
	 * et effectue des actions en fonction du type de message.
	 */
	@Override
	public void visitPrivateMsg(PrivateMsgFrame frame) {
		try {
			// ajouter la nouvelle src
			client.addPrivateClient(frame.getSrc(), frame.getToken(), key);
			client.writeMsg(frame);
		} catch (IOException e) {
			return;
		}
	}

}
