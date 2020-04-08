package chatHack.visitor;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import chatHack.client.ClientChatHack;
import chatHack.context.ServerContext;
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
 * a un client ChatHack de recevoir ou d'envoyer des frames au serveur
 */
public class ServerVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ClientChatHack client;

	/**
	 * Cree un objet de type ServerVisitor.
	 * @param key, la SelectionKey associee au serveur.
	 * @param client, le ClientChatHack associe au serveur.
	 */
	public ServerVisitor(SelectionKey key, ClientChatHack client) {
		this.key = key;
		this.client = client;
	}

	@Override
	public void visitGlobalMsgFrame(GlobalMsgFrame frame) {
		System.out.println(frame);
	}

	@Override
	public void visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
		System.out.println(frame);
	}

	/**
	 * Visite une frame de deconnexion
	 * et ferme le client.
	 */
	@Override
	public void visitLogOutFrame(LogOutFrame frame) {
		System.out.println(frame);
		ServerContext ctx = (ServerContext) key.attachment();
		if (ctx == null) {
			return;
		}
		ctx.close();
		ctx.silentlyClose();
		client.disconnect();
	}

	@Override
	public void visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
		System.out.println(frame);
	}

	/**
	 * Visite une frame d'acceptation d'une demande de connexion privee
	 * et connecte le client a un autre client en prive.
	 */
	@Override
	public void visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		try {
			System.out.println(frame);
			client.connectToClient(frame.getDst(), frame.getIp(), frame.getPort(), frame.getToken());
		} catch (IOException e) {
			return;
		}
	}

	@Override
	public void visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println(frame);
	}

	@Override
	public void visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		System.out.println(frame);
	}

	/**
	 * Visite une frame de demande de connexion privee
	 * et enregistre le client qui a demande la connexion privee.
	 */
	@Override
	public void visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		System.out.println(frame);
		client.addSrc(frame.getSrc());
	}

	@Override
	public void visitSimpleMsgFrame(SimpleMsgFrame frame) {
		System.out.println(frame);
	}

	/**
	 * Visite une frame de reponse d'authentification du serveur MDP
	 * et connecte le client au serveur ChatHack.
	 */
	@Override
	public void visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
		try {
			if ((client.withPassword() && frame.getOpcode() == 1)
					|| (!client.withPassword() && frame.getOpcode() == 0)) {
				System.out.println("you are connected\n");
				System.out.println("global msg:  @ msg OR / msg");
				System.out.println("private msg: @dest 0 msg [for txt msg]");
				System.out.println("             @dest 1 fileName [for file sending]");
				System.out.println("logout:      logout\n");
				ServerContext ctx = (ServerContext) key.attachment();

				if (ctx == null) {
					return;
				}
			} else {
				client.sendLogout();
			}
		} catch (InterruptedException e) {
			return;
		}

	}

	@Override
	public void visitPrivateMsg(PrivateMsgFrame frame) {
	}

}
