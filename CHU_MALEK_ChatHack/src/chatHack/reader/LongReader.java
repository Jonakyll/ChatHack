package chatHack.reader;

import java.nio.ByteBuffer;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet de stocker un entier long
 * a partir d'un ByteBuffer.
 */
public class LongReader implements Reader<Long> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private long value;

	/**
	 * Cree un objet de type LongReader.
	 * @param bb, le ByteBuffer a ananlyser.
	 */
	public LongReader(ByteBuffer bb) {
		this.bb = bb;
	}

	/**
	 * Lis le ByteBuffer et stocke un entier long.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		if (bb.remaining() >= Long.BYTES) {
			value = bb.getLong();
			state = State.DONE;
			return ProcessStatus.DONE;
		} else {
			return ProcessStatus.REFILL;
		}
	}

	/**
	 * Renvoie l'entier long stocke.
	 */
	@Override
	public Long get() {
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
