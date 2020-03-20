package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.SimpleMsgFrame;

public class PrivateMsgCnxReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private byte step;
	private String dst;

	private final StringReader dstReader;

	public PrivateMsgCnxReader(ByteBuffer bb) {
		this.bb = bb;
		this.dstReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING:
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

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateMsgCnxToDstFrame(step, dst);

		// verifier le cas ou la trame est mauvaise
//		return new SimpleMsgFrame((byte) 4, "your request couldn't reach your recipient");
	}

	@Override
	public void reset() {
		dstReader.reset();
		state = State.WAITING;
	}

}
