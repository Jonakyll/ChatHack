package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogOutPrivateFrame;
import chatHack.frame.LogOutPublicFrame;

public class LogOutToClientReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING_TYPE,
		WAITING_MSG,
		ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_TYPE;
	private byte logOutType;
	private String msg;

	private final ByteReader logOutTypeReader;
	private final StringReader msgReader;

	public LogOutToClientReader(ByteBuffer bb) {
		this.bb = bb;
		this.logOutTypeReader = new ByteReader(bb);
		this.msgReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_TYPE:
			ProcessStatus logOutTypeStatus = logOutTypeReader.process();
			if (logOutTypeStatus != ProcessStatus.DONE) {
				return logOutTypeStatus;
			}
			logOutType = logOutTypeReader.get();
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
		if (logOutType == 0) {
			return new LogOutPublicFrame(msg);
		}
		return new LogOutPrivateFrame(msg);
	}

	@Override
	public void reset() {
		logOutTypeReader.reset();
		msgReader.reset();
		state = State.WAITING_TYPE;
	}

}
