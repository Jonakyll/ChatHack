package chatHack.reader;

import java.nio.ByteBuffer;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'infertace Reader qui permet d'obtenir un byte dans un ByteBuffer.
 */
public class ByteReader implements Reader<Byte> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private byte value;

	/**
	 * Cree un objet de type ByteReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public ByteReader(ByteBuffer bb) {
		this.bb = bb;
	}

	/**
	 * Lis le ByteBuffer et stocke la valeur d'un byte.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		if (bb.remaining() >= Byte.BYTES) {
			value = bb.get();
			state = State.DONE;
			return ProcessStatus.DONE;
		} else {
			return ProcessStatus.REFILL;
		}
	}

	/**
	 * Renvoie la valeur du byte stockee.
	 */
	@Override
	public Byte get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		state = State.WAITING;
	}

}
