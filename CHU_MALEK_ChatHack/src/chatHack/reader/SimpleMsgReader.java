package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.SimpleMsgFrame;

public class SimpleMsgReader implements Reader<Frame> {

	public enum State {
		DONE, WAITING, ERROR
	};

//	private final ByteBuffer bb;
	private State state = State.WAITING;
	private String msg;

	private final StringReader msgReader;

	public SimpleMsgReader(ByteBuffer bb) {
//		this.bb = bb;
		this.msgReader = new StringReader(bb);
	}

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

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new SimpleMsgFrame((byte) 0, msg);
	}

	@Override
	public void reset() {
		msgReader.reset();
		state = State.WAITING;
	}

}
