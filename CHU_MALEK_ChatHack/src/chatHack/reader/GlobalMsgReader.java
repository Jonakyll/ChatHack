package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.GlobalMsgFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un objet Frame
 * contenant un message global a partir d'un ByteBuffer.
 */
public class GlobalMsgReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_EXP, WAITING_MSG, ERROR
	};

	private State state = State.WAITING_EXP;
	private String exp;
	private String msg;

	private final StringReader expReader;
	private final StringReader msgReader;

	/**
	 * Cree un objet de type GlobalMsgReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public GlobalMsgReader(ByteBuffer bb) {
		this.expReader = new StringReader(bb);
		this.msgReader = new StringReader(bb);
	}

	/**
	 * Lis le ByteBuffer et stocke les informations liees a un envoi de message global.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {
		case WAITING_EXP: {
			ProcessStatus expStatus = expReader.process();
			if (expStatus != ProcessStatus.DONE) {
				return expStatus;
			}
			exp = expReader.get();
			state = State.WAITING_MSG;
		}

		case WAITING_MSG: {
			ProcessStatus msgStatus = msgReader.process();
			if (msgStatus != ProcessStatus.DONE) {
				return msgStatus;
			}
			msg = msgReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;
		}

		default:
			throw new AssertionError();
		}
	}

	/**
	 * Renvoie une Frame de message global.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new GlobalMsgFrame(exp, msg);
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		expReader.reset();
		msgReader.reset();
		state = State.WAITING_EXP;
	}

}
