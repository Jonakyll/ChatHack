package chatHack.reader;

import java.nio.ByteBuffer;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un entier
 * a partir d'un ByteBuffer.
 */
public class IntReader implements Reader<Integer> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private int value;

	/**
	 * Cree un objet de type IntReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public IntReader(ByteBuffer bb) {
		this.bb = bb;
	}

	/**
	 * Lis le ByteBuffer et stocke la valeur d'un entier.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		if (bb.remaining() >= Integer.BYTES) {
			value = bb.getInt();
			state = State.DONE;
			return ProcessStatus.DONE;
		} else {
			return ProcessStatus.REFILL;
		}
	}

	/**
	 * Renvoie l'entier stocke.
	 */
	@Override
	public Integer get() {
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
