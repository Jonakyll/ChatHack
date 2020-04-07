package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

/**
 * 
 * @author CHU Jonathan
 * Interface representant les differentes frames echangees entre les clients et le serveur ChatHack
 * ou entre clients connectes en prive, ou entre le serveur ChatHack et le serveur MDP.
 */
public interface Frame {

	/**
	 * Appelle la methode associee au type de frame.
	 * @param visitor, un objet de l'interface FrameVisitor permettant d'appeler la bonne methode du visitor.
	 */
	void accept(FrameVisitor visitor);
	
	/**
	 * Construit un ByteBuffer correspondant a une frame;
	 * @return le ByteBuffer de la frame.
	 */
	ByteBuffer getByteBuffer();

}
