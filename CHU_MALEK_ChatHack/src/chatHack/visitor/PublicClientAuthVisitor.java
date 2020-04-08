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

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface FrameVisitor qui permet
 * au serveur ChatHack de recevoir ou d'envoyer des frames a un client authentifié
 */
public class PublicClientAuthVisitor implements FrameVisitor {

	private final SelectionKey key;
	private final ServerChatHack server;

	/**
	 * Cree un objet de type PublicClientAuthVisitor.
	 * @param key, la SelectionKey associee au client connecte.
	 * @param server, le ServerChatHack associe au client connecte.
	 */
	public PublicClientAuthVisitor(SelectionKey key, ServerChatHack server) {
		this.key = key;
		this.server = server;
	}

	/**
	 * Visite une frame de message globale et la diffuse
	 * a tous les clients connectes au meme serveur ChatHack.
	 */
	@Override
	public void visitGlobalMsgFrame(GlobalMsgFrame frame) {
		System.out.println("global");
		ByteBuffer buff = frame.getByteBuffer();
		
		// a envoyer a tout le monde
		server.broadcast(key, buff);
	}

	@Override
	public void visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame) {
	}

	/**
	 * Visite une frame de deconnexion et renvoie
	 * une nouvelle frame au client pour qu'il se deconnecte.
	 */
	@Override
	public void visitLogOutFrame(LogOutFrame frame) {
		System.out.println("log out");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui se deconnecte
		server.removeClient(key, buff);
	}

	@Override
	public void visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame) {
	}

	/**
	 * Visite une frame d'acceptation d'une demande de connexion privee
	 * et envoie la reponse au client qui a initie la demande.
	 */
	@Override
	public void visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame) {
		System.out.println("private msg cnx accepted to client");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui a envoye la frame
		server.sendToClientString(frame.getSrc(), buff);
	}

	/**
	 * Visite une frame de refus de connexion privee
	 * et envoie la reponse au client qui a initie la demande.
	 */
	@Override
	public void visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame) {
		System.out.println("private msg cnx refused to cliend");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au client qui a envoye la frame
		server.sendToClientString(frame.getSrc(), buff);
	}

	@Override
	public void visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame) {
		System.out.println("private msg cnx refused to server");
	}

	/**
	 * Visite une frame de demande de connexion privee
	 * et envoie la demande au client destinaire.
	 */
	@Override
	public void visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame) {
		System.out.println("private msg cnx to dst");
		ByteBuffer buff = frame.getByteBuffer();

		// a envoyer au destinataire
		server.sendToClientString(frame.getDst(), buff);
	}

	@Override
	public void visitSimpleMsgFrame(SimpleMsgFrame frame) {
		System.out.println("simple msg");
	}

	@Override
	public void visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame) {
	}

	@Override
	public void visitPrivateMsg(PrivateMsgFrame frame) {
	}

}
