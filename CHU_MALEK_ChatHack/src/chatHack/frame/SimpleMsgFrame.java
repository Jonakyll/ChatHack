package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant un simple message du serveur ChatHack.
 */
public class SimpleMsgFrame implements Frame {
	
	private final byte opcode;
	private final String msg;
	
	/**
	 * Cree un objet de type SimpleMsgFrame.
	 * @param opcode, le code pour le type de frame envoye.
	 * @param msg, le message a lire.
	 */
	public SimpleMsgFrame(byte opcode, String msg) {
		this.opcode = opcode;
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	/**
	 * Appelle la methode visitSimpleMsgFrame;
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitSimpleMsgFrame(this);
	}
	
	/**
	 * Construit un ByteBuffer du simple message a envoyer par le serveur ChatHack.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());

		buff.put(opcode);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie le message envoye par le serveur.
	 * @return le message envoye par le serveur.
	 */
	public String getMsg() {
		return msg;
	}
	
	/**
	 * Renvoie l'opcode de la frame.
	 * @return l'opcode de la frame.
	 */
	public byte getOpcode() {
		return opcode;
	}

}
