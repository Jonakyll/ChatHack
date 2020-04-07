package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant un refus d'un client
 * d'une demande de connexion privee (vers le serveur ChatHack).
 */
public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	/**
	 * Cree un objet de type PrivateMsgCnxRefusedToServerFrame.
	 */
	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
	/**
	 * Appelle la methode visitPrivateMsgCnxRefusedToServerFrame.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToServerFrame(this);
	}

	/**
	 * Construit le ByteBuffer de refus de connexion privee (a envoyer au serveur ChatHack).
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();
		
		return buff;
	}

}
