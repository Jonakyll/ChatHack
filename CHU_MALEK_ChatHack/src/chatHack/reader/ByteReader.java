package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;

public class ByteReader implements Reader {
	
	private enum State {
		DONE,
		WAITING,
		ERROR
	};
	
	private final ByteBuffer bb;
	private State state = State.WAITING;
	private byte value;
	
	public ByteReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();
		
		try {
			if (bb.remaining() >= Byte.BYTES) {
				value = bb.get();
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			else {
				return ProcessStatus.REFILL;
			}
		} finally {
			bb.compact();
		}
	}

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
