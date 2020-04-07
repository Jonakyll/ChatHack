package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant un refus de connexion
 * privee vers un client.
 */
public class PrivateMsgCnxRefusedToClientFrame implements Frame {

	private final String src;
	private final String dst;
	private final String errMsg;

	/**
	 * Cree un objet de type PrivateMsgCnxRefusedToClientFrame.
	 * @param src, le pseudo du client qui a demande la connexion privee.
	 * @param dst, le pseudo du client destinataire.
	 * @param errMsg, le message de refus.
	 */
	public PrivateMsgCnxRefusedToClientFrame(String src, String dst, String errMsg) {
		this.src = src;
		this.dst = dst;
		this.errMsg = errMsg;
	}

	@Override
	public String toString() {
		return errMsg;
	}

	/**
	 * Appelle la methode visitPrivateMsgCnxRefusedToClientFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToClientFrame(this);
	}
	
	/**
	 * Construit un ByteBuffer du refus de connexion privee.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(errMsg);
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 3 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining() + errMsgBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.putInt(errMsgBuff.remaining());
		buff.put(errMsgBuff);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie le pseudo du client qui a fait la demande de connexion privee.
	 * @return le pseudo du client qui a fait la demande de connexion privee.
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
	 * Renvoie le message de refus.
	 * @return le message de refus.
	 */
	public String getErrMsg() {
		return errMsg;
	}

}
