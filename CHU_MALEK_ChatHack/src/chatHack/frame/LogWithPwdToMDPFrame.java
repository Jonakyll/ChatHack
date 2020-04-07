package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Frame representant une demande d'authentification
 * d'un client avec mot de passe.
 */
public class LogWithPwdToMDPFrame implements Frame {

	private final String name;
	private final String password;
	private long id;

	/**
	 * Cree un objet de type LogWithPwdToMDPFrame.
	 * @param name, le pseudo du client qui veut s'authentifier.
	 * @param password, son mot de passe.
	 */
	public LogWithPwdToMDPFrame(String name, String password) {
		this.name = name;
		this.password = password;
	}

	/**
	 * Appelle la methode visitLogWithPwdToMDPFrame du visitor.
	 */
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogWithPwdToMDPFrame(this);
	}

	/**
	 * Construit un ByteBuffer d'une demande d'authentification a envoyer au server MDP.
	 */
	@Override
	public ByteBuffer getByteBuffer() {
		Random random = new Random();
		id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(password);
		ByteBuffer buff = ByteBuffer.allocate(
				Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());

		buff.put((byte) 1);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.putInt(passwordBuff.remaining());
		buff.put(passwordBuff);
		buff.flip();
		
		return buff;
	}

	/**
	 * Renvoie le pseudo du client qui veut s'authentifier.
	 * @return le pseudo du client qui veut s'authentifier.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Renvoie son mot de passe.
	 * @return son mot de passe.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Renvoie l'identifiant de la demande d'authentification.
	 * @return l'identifiant de la demande d'authentification.
	 */
	public long getId() {
		return id;
	}

}
