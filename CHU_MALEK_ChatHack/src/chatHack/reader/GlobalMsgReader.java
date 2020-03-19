package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.GlobalMsgFrame;

public class GlobalMsgReader implements Reader<Frame> {
	
	private enum State {
		DONE,
		WAITING_EXP,
		WAITING_MSG,
		ERROR
	};
	
	private final ByteBuffer bb;
	private State state = State.WAITING_EXP;
	private String exp;
	private String msg;
	
	private final StringReader expReader;
	private final StringReader msgReader;
	
	public GlobalMsgReader(ByteBuffer bb) {
		this.bb = bb;
		this.expReader = new StringReader(bb);
		this.msgReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		switch (state) {
		case WAITING_EXP: {
			ProcessStatus expStatus = expReader.process();
			if (expStatus != ProcessStatus.DONE) {
				return expStatus;
			}
			exp = expReader.get();
			state = State.WAITING_MSG;
		}
		
		case WAITING_MSG: {
			ProcessStatus msgStatus = msgReader.process();
			if (msgStatus != ProcessStatus.DONE) {
				return msgStatus;
			}
			msg = msgReader.get();
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
		return new GlobalMsgFrame(exp, msg);
		
//		verifier le cas ou la trame est mauvaise (taille du msg != du msg reel)
	}

	@Override
	public void reset() {
		expReader.reset();
		msgReader.reset();
		state = State.WAITING_EXP;
	}

}
