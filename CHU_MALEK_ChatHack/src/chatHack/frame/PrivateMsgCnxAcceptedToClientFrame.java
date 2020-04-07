package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant une acceptation
 * d'une demande de connexion privee d'un client.
 */
public class PrivateMsgCnxAcceptedToClientFrame implements Frame {
	
	private final String src;
	private final String dst;
	private final int port;
	private final long token;
	private final String ip;
	
	/**
	 * Cree un objet de type PrivateMsgCnxAcceptedToClientFrame.
	 * @param src, le pseudo du client qui demande la connexion privee.
	 * @param dst, le pseudo du client destinataire.
	 * @param port, le port sur lequel le destinataire est libre pour une discussion privee.
	 * @param token, le token a utiliser pour pouvoir recevoir ou envoyer des messages prives vers la bonne personne.
	 * @param ip, l'adresse ip du destinaire.
	 */
	public PrivateMsgCnxAcceptedToClientFrame(String src, String dst, int port, long token, String ip) {
		this.src = src;
		this.dst = dst;
		this.port = port;
		this.token = token;
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "you can now open a connection to " + dst + ": " + ip + " on port " + port;
	}

	/**
	 * Appelle la methode visitPrivateMsgCnxAcceptedToClientFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxAcceptedToClientFrame(this);
	}
	
	/**
	 * Construit le ByteBuffer de l'acceptation du client a une demande de connexion privee.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer ipBuff = StandardCharsets.UTF_8.encode(ip);
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 4 * Integer.BYTES + Long.BYTES + srcBuff.remaining() + dstBuff.remaining() + ipBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.putInt(port);
		buff.putLong(token);
		buff.putInt(ipBuff.remaining());
		buff.put(ipBuff);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie le pseudo de la personne qui demande la connexion privee.
	 * @return le pseudo de la personne qui demande la connexion privee.
	 */
	public String getSrc() {
		return src;
	}
	
	/**
	 * Renvoie le pseudo du client destinataire.
	 * @return le pseudo du client destinataire.
	 */
	public String getDst() {
		return dst;
	}
	
	/**
	 * Renvoie l'adresse ip du client destinataire.
	 * @return l'adresse ip du client destinataire.
	 */
	public String getIp() {
		return ip;
	}
	
	/**
	 * Renvoie le port du client destinataire.
	 * @return le port du client destinataire.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Renvoie le token de discussion.
	 * @return le token de discussion.
	 */
	public long getToken() {
		return token;
	}
}
