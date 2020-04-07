package chatHack.reader;

import chatHack.frame.Frame;
import chatHack.frame.LogOutFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir une Frame
 * de deconnexion a envoyer au server ChatHack.
 */
public class LogOutToServerReader implements Reader<Frame> {

	/**
	 * 
	 */
	@Override
	public ProcessStatus process() {
		return ProcessStatus.DONE;
	}

	/**
	 * Renvoie une Frame de deconnexion.
	 */
	@Override
	public Frame get() {
		return new LogOutFrame("you have been disconnected from the public server");

	}

	/**
	 * 
	 */
	@Override
	public void reset() {
	}

}
