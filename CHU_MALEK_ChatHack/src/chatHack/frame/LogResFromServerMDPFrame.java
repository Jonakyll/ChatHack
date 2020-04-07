package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant la reponse du serveur MDP
 * d'une demande d'authentification.
 */
public class LogResFromServerMDPFrame implements Frame {
	
	private final byte opcode;
	private final long id;
	
	/**
	 * Cree un objet de type LogResFromServerMDPFrame.
	 * @param opcode, le type de reponse du serveur MDP.
	 * @param id, l'identifiant de la demande d'authentification.
	 */
	public LogResFromServerMDPFrame(byte opcode, long id) {
		this.opcode = opcode;
		this.id = id;
	}

	/**
	 * Appelle la methode visitLogResFromServerMDPFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogResFromServerMDPFrame(this);
	}
	
	/**
	 * Construit le ByteBuffer de la reponse du serveur MDP.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);

		buff.put(opcode);
		buff.putLong(id);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie l'opcode de la reponse.
	 * @return l'opcode de la reponse.
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	/**
	 * Renvoie l'identifiant de la reponse.
	 * @return l'identifiant de la reponse.
	 */
	public long getId() {
		return id;
	}

}
