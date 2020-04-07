package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant une demande de connexion privee
 * que le serveur enverra au client destinataire.
 */
public class PrivateMsgCnxToDstFrame implements Frame {

	private final byte step;
	private final String src;
	private final String dst;

	/**
	 * Cree un objet de type PrivateMsgCnxToDstFrame.
	 * @param step, l'etape de la demande de connexion privee.
	 * @param src, le pseudo du client qui demande la connexion privee.
	 * @param dst, le pseudo du client destinataire.
	 */
	public PrivateMsgCnxToDstFrame(byte step, String src, String dst) {
		this.step = step;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public String toString() {
		return src + " wants to start a private conversation with you.\n\n0     = accept\nother = decline";
	}

	/**
	 * Appelle la methode visitPrivateMsgCnxToDstFrame.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxToDstFrame(this);
	}

	/**
	 * Construit le ByteBuffer de demande de connexion privee a envoyer au client destinataire.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer buff = ByteBuffer
				.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();
		
		return buff;
	}

	/**
	 * Renvoie le pseudo du client qui demande la connexion privee.
	 * @return le pseudo du client qui demande la connexion privee.
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
	 * Renvoie l'etape de la demande de connexion.
	 * @return l'etape de la demande de connexion.
	 */
	public byte getStep() {
		return step;
	}
}
