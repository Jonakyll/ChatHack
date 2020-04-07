package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'infertafe Frame representant une frame d'authentification sans mot de passe
 * venant du serveur ChatHack vers le serveur MDP.
 */
public class LogNoPwdToMDPFrame implements Frame {

	private final String name;
	private long id;
	
	/**
	 * Cree un objet de type LogNoPwdToMDPFrame.
	 * @param name, le pseudo du client qui veut s'authentifier.
	 */
	public LogNoPwdToMDPFrame(String name) {
		this.name = name;
	}

	/**
	 * Appelle la methode visitLogNoPwdToMDPFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogNoPwdToMDPFrame(this);
	}
	
	/**
	 * Construit le ByteBuffer de la demande d'authentification d'un client sans mot de passe.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		Random random = new Random();
		id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + nameBuff.remaining());

		buff.put((byte) 2);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.flip();
		
		return buff;
	}
	
	/**
	 * Renvoie le pseudo du client qui veut s'authentifier.
	 * @return son pseudo.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Renvoie l'identifiant de la frame.
	 * @return l'identifiant de la frame.
	 */
	public long getId() {
		return id;
	}


}
