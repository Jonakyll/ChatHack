package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxToDstFrame;

public class PrivateMsgCnxReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING_STEP,
		WAITING_DST,
		ERROR
	};
	
	private final ByteBuffer bb;
	private State state = State.WAITING_STEP;
	private byte step;
	private String dst;
	
	private final ByteReader stepReader;
	private final StringReader dstReader;
	
	public PrivateMsgCnxReader(ByteBuffer bb) {
		this.bb = bb;
		this.stepReader = new ByteReader(bb);
		this.dstReader = new StringReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		switch (state) {
		case WAITING_STEP: {
			ProcessStatus stepStatus = stepReader.process();
			if (stepStatus != ProcessStatus.DONE) {
				return stepStatus;
			}
			step = stepReader.get();
			state = State.WAITING_DST;
		}
		
		case WAITING_DST: {
			ProcessStatus dstStatus = dstReader.process();
			if (dstStatus != ProcessStatus.DONE) {
				return dstStatus;
			}
			dst = dstReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;
		}
		
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
		
//		verifier le cas ou la trame est mauvaise
	}

	@Override
	public void reset() {
		state = State.WAITING_STEP;
	}

}
