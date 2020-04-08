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

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface FrameVisitor qui permet
 * au serveur ChatHack de recevoir des frames de demande de connexion
 * de clients et d'envoyer des frames d'authentification au serveur MDP.
 */
public class PublicClientNotAuthVisitor implements FrameVisitor {

	private final ServerChatHack server;
	private final PublicClientContext ctx;

	/**
	 * Cree un objet de type PublicClientNotAuthVisitor.
	 * @param server, le ServerChatHack associe au client non connecte.
	 * @param ctx, le PublicClientContext associe au client non connecte.
	 */
	public PublicClientNotAuthVisitor(ServerChatHack server, PublicClientContext ctx) {
		this.server = server;
		this.ctx = ctx;
	}

	@Override
	public void visitGlobalMsgFrame(GlobalMsgFrame frame) {
	}

	/**
	 * Visite une frame de demande de connexion sans mot de passe
	 * et envoie une frame d'authentification au serveur MDP.
	 */
	@Override
	public void visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println("log no pwd to mdp");
		ByteBuffer buff = frame.getByteBuffer();
		
		server.addClientString(frame.getName(), ctx);
		server.addClientLong(frame.getId(), ctx);

		// a envoyer au serveur MDP
		server.sendToMDP(buff);
	}

	@Override
	public void visitLogOutFrame(LogOutFrame frame) {
	}

	/**
	 * Visite une frame de demande de connexion avec mot de passe
	 * et envoie une frame d'authentification au serveur MDP.
	 */
	@Override
	public void visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println("log with pwd to mdp");
		ByteBuffer buff = frame.getByteBuffer();
		
		server.addClientString(frame.getName(), ctx);
		server.addClientLong(frame.getId(), ctx);

		// a envoyer au serveur MDP
		server.sendToMDP(buff);
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

	@Override
	public void visitPrivateMsg(PrivateMsgFrame frame) {
	}
}
