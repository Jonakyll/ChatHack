package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant un message global
 * venant d'un client connecte au serveur ChatHack.
 */
public class GlobalMsgFrame implements Frame {

	private final String exp;
	private final String msg;

	/**
	 * Cree un objet de type GlobalMsgFrame.
	 * @param exp, le pseudo de l'expediteur du message.
	 * @param msg, le message a transmettre aux clients.
	 */
	public GlobalMsgFrame(String exp, String msg) {
		this.exp = exp;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return exp + ": " + msg;
	}

	/**
	 * Appelle la methode visitGlobalMsgFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitGlobalMsgFrame(this);
	}

	/**
	 * Construit le ByteBuffer du message global.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer expBuff = StandardCharsets.UTF_8.encode(exp);
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer
				.allocate(Byte.BYTES + 2 * Integer.BYTES + expBuff.remaining() + msgBuff.remaining());

		buff.put((byte) 3);
		buff.putInt(expBuff.remaining());
		buff.put(expBuff);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();

		return buff;
	}

	/**
	 * Renvoie le pseudo de l'expediteur du message.
	 * @return le pseudo de l'expediteur du message.
	 */
	public String getExp() {
		return exp;
	}

	/**
	 * Renvoie le message a diffuser.
	 * @return le message a diffuser.
	 */
	public String getMsg() {
		return msg;
	}


}
