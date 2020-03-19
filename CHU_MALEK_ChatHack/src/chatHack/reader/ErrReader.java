package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.ErrFrame;
import chatHack.frame.Frame;

public class ErrReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING,
		ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private String msg;

	private final StringReader msgReader;

	public ErrReader(ByteBuffer bb) {
		this.bb = bb;
		this.msgReader = new StringReader(bb);
	}

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

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new ErrFrame(msg);
	}

	@Override
	public void reset() {
		msgReader.reset();
		state = State.WAITING;
	}

}
