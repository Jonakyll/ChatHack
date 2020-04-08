package chatHack.visitor;

import java.nio.ByteBuffer;

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

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface FrameVisitor qui permet
 * au serveur ChatHack de recuperer des frames venant d'un serveur MDP
 * d'envoyer la reponse au client qui a demander a se connecter.
 */
public class MDPVisitor implements FrameVisitor {

	private final ServerChatHack server;
	
	/**
	 * Cree un objet de type MDPVisitor.
	 * @param server, le ServerChatHack connecte au serveur MDP.
	 */
	public MDPVisitor(ServerChatHack server) {
		this.server = server;
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

	/**
	 * Visite une frame de reponse d'authentification du serveur MDP
	 * et l'envoie au client qui a demande a se connecter au serveur ChatHack.
	 */
	@Override
	public void visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
		System.out.println("server mdp res");
		ByteBuffer buff = frame.getByteBuffer();
		
		// a envoyer au client
		server.sendToClientLong(frame.getId(), buff);
	}

	@Override
	public void visitPrivateMsg(PrivateMsgFrame frame) {
	}

}
