package chatHack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * 
 * @author MALEK Akram
 * Interface liee a une SelectionKey afin de pouvoir lire ou envoyer des donnees vers differentes sources.
 */
public interface Context {

	/**
	 * Lis les donnees recues par une autre source.
	 */
	void processIn();
	
	/**
	 * Ajoute un ByteBuffer a une liste de ByteBuffer a envoyer.
	 * @param buff, un ByteBuffer contenant des donnees.
	 */
	void queueFrame(ByteBuffer buff);
	
	/**
	 * Prepare le ByteBuffer des donnees a envoyer vers une source.
	 */
	void processOut();
	
	/**
	 * Met a jour les operations possibles sur les donnees.
	 */
	void updateInterestOps();
	
	/**
	 * Ferme la SelectionKey liee a l'objet Context.
	 */
	void silentlyClose();
	
	/**
	 * Connecte un Channel a un serveur.
	 * @throws IOException
	 */
	void doConnect() throws IOException;
	
	/**
	 * Envoie des donnees vers une autre source.
	 * @throws IOException
	 */
	void doWrite() throws IOException;
	
	/**
	 * Lis les donnees recues par une autre source.
	 * @throws IOException
	 */
	void doRead() throws IOException;
	
	/**
	 * Renvoie la SelectionKey associee a l'objet Context.
	 * @return la SelectionKey associee a l'objet Context.
	 */
	SelectionKey getKey();

}
