package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxToDstFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un objet Frame
 * d'une demande de connexion a une discussion privee a partir d'un ByteBuffer.
 */
public class PrivateMsgCnxReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_SRC, WAITING_DST, ERROR
	};

	private State state = State.WAITING_SRC;
	private byte step;
	private String src;
	private String dst;

	private final StringReader srcReader;
	private final StringReader dstReader;

	/**
	 * Cree un objet de typte PrivateMsgCnxReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public PrivateMsgCnxReader(ByteBuffer bb) {
		this.srcReader = new StringReader(bb);
		this.dstReader = new StringReader(bb);
	}

	/**
	 * Lis le ByteBuffer et stocke les informations liees
	 * a la demande de connexion a une discussion privee.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_SRC:
			ProcessStatus srcStatus = srcReader.process();
			if (srcStatus != ProcessStatus.DONE) {
				return srcStatus;
			}
			src = srcReader.get();
			state = State.WAITING_DST;

		case WAITING_DST:
			ProcessStatus dstStatus = dstReader.process();
			if (dstStatus != ProcessStatus.DONE) {
				return dstStatus;
			}
			dst = dstReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;
			
		default:
			throw new AssertionError();
		}
	}

	/**
	 * Renvoie une Frame de message prive.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateMsgCnxToDstFrame(step, src, dst);
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		srcReader.reset();
		dstReader.reset();
		state = State.WAITING_SRC;
	}

}
