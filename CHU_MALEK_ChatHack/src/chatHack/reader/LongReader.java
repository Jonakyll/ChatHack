package chatHack.reader;

import java.nio.ByteBuffer;

public class LongReader implements Reader<Long> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private long value;

	public LongReader(ByteBuffer bb) {
		this.bb = bb;
	}

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

	@Override
	public Long get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	@Override
	public void reset() {
		state = State.WAITING;
	}

}
