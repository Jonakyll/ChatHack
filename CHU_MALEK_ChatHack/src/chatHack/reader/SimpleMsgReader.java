package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.SimpleMsgFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un objet Frame
 * de simple message a partir d'un ByteBuffer.
 */
public class SimpleMsgReader implements Reader<Frame> {

	public enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private String msg;

	private final StringReader msgReader;

	/**
	 * Cree un objet de type SimpleMsgReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public SimpleMsgReader(ByteBuffer bb) {
		this.msgReader = new StringReader(bb);
	}

	/**
	 * Lis le ByteBuffer et stocke les informations liees
	 * au simple message.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		ProcessStatus msgStatus = msgReader.process();
		if (msgStatus != ProcessStatus.DONE) {
			return msgStatus;
		}
		msg = msgReader.get();
		state = State.DONE;
		return ProcessStatus.DONE;
	}

	/**
	 * Renvoie une Frame de simple message.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new SimpleMsgFrame((byte) 0, msg);
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
