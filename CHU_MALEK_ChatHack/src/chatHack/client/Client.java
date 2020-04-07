package chatHack.client;

import java.nio.channels.SelectionKey;


/**
 * 
 * @author MALEK Akram
 * Objet representant un client souhaitant ou ayant deja etabli une connexion privee avec un autre client.
 */
public class Client {
	
	private final long token;
	private final SelectionKey key;
	
	/**
	 * Cree un objet de type Client.
	 * @param token, entier long permettant a 2 clients connnectes en prive de s'echanger des messages
	 * sans qu'un autre client non autorise puisse envoyer de messages dans cette conversation privee.
	 * @param key, SelectionKey reliee a un client prive permettant au Selector d'un client de traiter les donnees qu'il envoie ou qu'il reçoit.
	 */
	public Client(long token, SelectionKey key) {
		this.token = token;
		this.key = key;
	}

	/**
	 * Renvoie le token permettant l'echange de messages entre 2 clients en prive.
	 * @return l'entier long correspondant au token.
	 */
	public long getToken() {
		return token;
	}
	
	/**
	 * Renvoie la SelectionKey permettant a un client d'envoyer ou de recevoir des messages venant du client lie a cette cle.
	 * @return la SelectionKey du client enregistre par un Selector.
	 */
	public SelectionKey getKey() {
		return key;
	}
}
