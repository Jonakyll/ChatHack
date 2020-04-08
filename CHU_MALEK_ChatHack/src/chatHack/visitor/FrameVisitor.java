package chatHack.visitor;

import chatHack.frame.GlobalMsgFrame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogOutFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToServerFrame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.PrivateMsgFrame;
import chatHack.frame.LogResFromServerMDPFrame;
import chatHack.frame.SimpleMsgFrame;

/**
 * 
 * @author CHU Jonathan
 * Interface permettant d'effectuer des actions
 * en fonction du type de Frame que le serveur ou le client ChatHack rencontrent.
 */
public interface FrameVisitor {

	/**
	 * Visite une frame de message global.
	 * @param frame, la Frame contenant le message global.
	 */
	void visitGlobalMsgFrame(GlobalMsgFrame frame);
	
	/**
	 * Visite une frame de demande de connexion sans mot de passe.
	 * @param frame, la frame de demande de connexion envoyee par un client.
	 */
	void visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame);
	
	/**
	 * Visite une frame de deconnexion.
	 * @param frame, la frame de deconnexion envoyee par un client.
	 */
	void visitLogOutFrame(LogOutFrame frame);
	
	/**
	 * Visite une frame de demande de connexion avec mot de passe.
	 * @param frame, la frame de demande de connexion envoyee par un client.
	 */
	void visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame);
	
	/**
	 * Visite une frame d'acceptation d'une demande de connexion privee.
	 * @param frame, la frame de reponse du client destinataire.
	 */
	void visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame);
	
	/**
	 * Visite une frame de refus de connexion privee.
	 * @param frame, la frame de refus du client destinataire.
	 */
	void visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame);
	
	/**
	 * Visite une frame de refus de connexion privee
	 * et envoie la reponse au serveur ChatHack.
	 * @param frame, la frame de refus du client destinataire.
	 */
	void visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame);
	
	/**
	 * Visite une frame de demande de connexion privee.
	 * @param frame, la frame de demande de connexion privee.
	 */
	void visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame);
	
	/**
	 * Visite une frame de simple message.
	 * @param frame, la frame de simple message.
	 */
	void visitSimpleMsgFrame(SimpleMsgFrame frame);

	/**
	 * Visite une frame de reponse d'authentification du serveur MDP.
	 * @param frame, la reponse du serveur MDP.
	 */
	void visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame);
	
	/**
	 * Visite une frame de message prive.
	 * @param frame, la frame de message prive.
	 */
	void visitPrivateMsg(PrivateMsgFrame frame);
}
