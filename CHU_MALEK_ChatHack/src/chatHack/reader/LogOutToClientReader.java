package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogOutFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet de stocker des informations liees
 * a une Frame de deconnexion envoye a un client ChatHack.
 */
public class LogOutToClientReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private String msg;

	private final StringReader msgReader;

	/**
	 * Cree un objet de type LogOutToClientReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public LogOutToClientReader(ByteBuffer bb) {
		this.msgReader = new StringReader(bb);
	}

	/**
	 * Lis le ByteBuffer et stocke les informations liees a la frame de deconnexion.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING:
			ProcessStatus msgStatus = msgReader.process();
			if (msgStatus != ProcessStatus.DONE) {
				return msgStatus;
			}
			msg = msgReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;

		default:
			throw new AssertionError();
		}
	}

	/**
	 * Renvoie une Frame de deconnexion.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new LogOutFrame(msg);
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		msgReader.reset();
		state = State.WAITING;
	}

}
