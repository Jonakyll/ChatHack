package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgFrame;

public class PrivateMsgReader implements Reader<Frame> {
	
	private enum State {
		DONE, WAITING_TYPE, WAITING_TOKEN, WAITING_MSG, ERROR
	};
	
	private State state = State.WAITING_TYPE;
	private byte type;
	private long token;
	private String msg;
	
	private final ByteReader typeReader;
	private final LongReader tokenReader;
	private final StringReader msgReader;
	
	public PrivateMsgReader(ByteBuffer bb) {
		this.typeReader = new ByteReader(bb);
		this.tokenReader = new LongReader(bb);
		this.msgReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		switch (state) {
		case WAITING_TYPE:
			ProcessStatus typeStatus = typeReader.process();
			if (typeStatus != ProcessStatus.DONE) {
				return typeStatus;
			}
			type = typeReader.get();
			state = State.WAITING_TOKEN;
			
		case WAITING_TOKEN:
			ProcessStatus tokenStatus = tokenReader.process();
			if (tokenStatus != ProcessStatus.DONE) {
				return tokenStatus;
			}
			token = tokenReader.get();
			state = State.WAITING_MSG;
			
		case WAITING_MSG:
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
		return new PrivateMsgFrame(token, msg);
	}

	@Override
	public void reset() {
		typeReader.reset();
		tokenReader.reset();
		msgReader.reset();
		state = State.WAITING_TYPE;
	}

}
