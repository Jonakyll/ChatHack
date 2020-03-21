package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;

public class FrameToClientReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private Reader<Frame> reader;
	private Frame frame;
	private byte opcode;

	public FrameToClientReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();

		try {

			if (bb.remaining() < Byte.BYTES) {
				return ProcessStatus.REFILL;
			}

			checkOpcode();

			ProcessStatus status = reader.process();
			if (status != ProcessStatus.DONE) {
				return status;
			}
			frame = reader.get();
			state = State.DONE;
			return ProcessStatus.DONE;
		} finally {
			bb.compact();
		}
	}

	private void checkOpcode() {
		opcode = bb.get();

		switch (opcode) {
		case 0:
			reader = new LogResFromMDPReader(opcode, bb);
			break;
			
		case 1:
			reader = new LogResFromMDPReader(opcode, bb);
			break;

		case 2:
			reader = new SimpleMsgReader(bb);
			break;

		case 3:
			reader = new GlobalMsgReader(bb);
			break;

		case 4:
			checkStep();
			break;

		case 5:
			reader = new LogOutToClientReader(bb);
			break;

		case 6:
			reader = new SimpleMsgReader(bb);
			break;

		default:
			throw new AssertionError();
		}
	}

	private void checkStep() {
		if (bb.remaining() >= Byte.BYTES) {

			byte step = bb.get();

			switch (step) {
			case 0:
				reader = new PrivateMsgCnxReader(bb);
				break;

			case 1:
				reader = new PrivateMsgCnxResToClientReader(bb);
				break;

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
		return frame;
	}

	@Override
	public void reset() {
		reader.reset();
		state = State.WAITING;
	}

}
