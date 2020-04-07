package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant un message prive
 * echange entre clients.
 */
public class PrivateMsgFrame implements Frame {

	private final byte type;
	private final long token;
	private final String src;
	private String fileName;
	private final String msg;

	/**
	 * Cree un objet de type PrivateMsgFrame.
	 * @param type, le type de message (textuel ou fichier).
	 * @param token, le token permettant de savoir si un client est autorise ou non a envoyer des messages prives.
	 * @param src, le pseudo du client qui envoie le message prive.
	 * @param msg, le message du client source.
	 */
	public PrivateMsgFrame(byte type, long token, String src, String msg) {
		this.type = type;
		this.token = token;
		this.src = src;
		this.msg = msg;
	}

	/**
	 * Cree un objet de type PrivateMsgFrame.
	 * @param type, le type de message (textuel ou fichier).
	 * @param token, le token permettant de savoir si un client est autorise ou non a envoyer des messages prives.
	 * @param src, le pseudo du client qui envoie le message prive.
	 $ @param fileName, le nom du fichier envoye.
	 * @param msg, le fichier envoye.
	 */
	public PrivateMsgFrame(byte type, long token, String src, String fileName, String msg) {
		this.type = type;
		this.token = token;
		this.src = src;
		this.fileName = fileName;
		this.msg = msg;
	}

	/**
	 * Appelle la methode visitPrivateMsg.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsg(this);
	}

	/**
	 * Construit le ByteBuffer du message prive.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		if (type == 0) {
			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
			ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
			ByteBuffer buff = ByteBuffer.allocate(
					3 * Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + msgBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 2);
			buff.put(type);
			buff.putLong(token);
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(msgBuff.remaining());
			buff.put(msgBuff);
			buff.flip();

			return buff;
		} else {
			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
			ByteBuffer fileNameBuff = StandardCharsets.UTF_8.encode(fileName);
			ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
			ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Long.BYTES + 3 * Integer.BYTES + srcBuff.remaining()
					+ fileNameBuff.remaining() + msgBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 2);
			buff.put(type);
			buff.putLong(token);
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(fileNameBuff.remaining());
			buff.put(fileNameBuff);
			buff.putInt(msgBuff.remaining());
			buff.put(msgBuff);
			buff.flip();

			return buff;
		}
	}

	@Override
	public String toString() {
		return src + ": " + msg;
	}

	/**
	 * Renvoie le type de message.
	 * @return le type de message.
	 */
	public byte getType() {
		return type;
	}

	/**
	 * Renvoie le token d'identification d'un client prive.
	 * @return le token d'identification d'un client prive.
	 */
	public long getToken() {
		return token;
	}

	/**
	 * Renvoie le pseudo du client source.
	 * @return le pseudo du client source.
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * Renvoie le nom du fichier envoye.
	 * @return le nom du fichier envoye.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Renvoie le message envoye.
	 * @return le message envoye.
	 */
	public String getMsg() {
		return msg;
	}

}
