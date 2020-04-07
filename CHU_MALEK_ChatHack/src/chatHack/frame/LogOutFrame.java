package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant
 * une reponse a une demande de deconnexion d'un client.
 */
public class LogOutFrame implements Frame {

	private final String msg;
	
	/**
	 * Cree un objet de type LogOutFrame.
	 * @param msg, le message de deconnexion.
	 */
	public LogOutFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	/**
	 * Appelle la methode visitLogOutFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogOutFrame(this);
	}
	
	/**
	 * Construit le ByteBuffer de deconnexion qui sera envoye au client qui a fait la demande.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());

		buff.put((byte) 5);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie le message de deconnexion.
	 * @return le message de deconnexion.
	 */
	public String getMsg() {
		return msg;
	}


}
