package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgFrame;

public class PrivateMsgReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_TYPE, WAITING_TOKEN, WAITING_SRC, WAITING_FILENAME, WAITING_MSG, ERROR
	};

	private State state = State.WAITING_TYPE;
	private byte type;
	private long token;
	private String src;
	private String fileName;
	private String msg;

	private final ByteReader typeReader;
	private final LongReader tokenReader;
	private final StringReader srcReader;
	private final StringReader fileNameReader;
	private final StringReader msgReader;

	public PrivateMsgReader(ByteBuffer bb) {
		this.typeReader = new ByteReader(bb);
		this.tokenReader = new LongReader(bb);
		this.srcReader = new StringReader(bb);
		this.fileNameReader = new StringReader(bb);
		this.msgReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {

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
				state = State.WAITING_SRC;

			case WAITING_SRC:
				ProcessStatus srcStatus = srcReader.process();
				if (srcStatus != ProcessStatus.DONE) {
					return srcStatus;
				}
				src = srcReader.get();
				if (type == 0) {
					state = State.WAITING_MSG;
					break;
				} else {
					state = State.WAITING_FILENAME;
					break;
				}

			case WAITING_FILENAME:
				ProcessStatus fileNameStatus = fileNameReader.process();
				if (fileNameStatus != ProcessStatus.DONE) {
					return fileNameStatus;
				}
				fileName = fileNameReader.get();
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
	}

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		if (type == 0) {
			return new PrivateMsgFrame(type, token, src, msg);
		} else {
			return new PrivateMsgFrame(type, token, src, fileName, msg);
		}
	}

	@Override
	public void reset() {
		typeReader.reset();
		tokenReader.reset();
		srcReader.reset();
		fileNameReader.reset();
		msgReader.reset();
		state = State.WAITING_TYPE;
	}

}
