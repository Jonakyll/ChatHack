package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;

public class PrivateMsgCnxAcceptedFromDstReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING_STEP,
		WAITING_RES_TYPE,
		WAITING_PORT,
		ERROR,
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_STEP;
	private byte step;
	private byte resType;
	private int port;

	private final ByteReader stepReader;
	private final ByteReader resTypeReader;
	private final IntReader portReader;

	public PrivateMsgCnxAcceptedFromDstReader(ByteBuffer bb) {
		this.bb = bb;
		this.stepReader = new ByteReader(bb);
		this.resTypeReader = new ByteReader(bb);
		this.portReader = new IntReader(bb);
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
			state = State.WAITING_RES_TYPE;
		}

		case WAITING_RES_TYPE: {
			ProcessStatus resTypeStatus = resTypeReader.process();
			if (resTypeStatus != ProcessStatus.DONE) {
				return resTypeStatus;
			}
			resType = resTypeReader.get();

			if (resType == 0) {
				state = State.WAITING_PORT;
			}
			else if (resType == 1) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			//			tester le cas d'erreur
		}

		case WAITING_PORT: {
			ProcessStatus portStatus = portReader.process();
			if (portStatus != ProcessStatus.DONE) {
				return portStatus;
			}
			port = portReader.get();
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

		if (resType == 0) {
			return new PrivateMsgCnxAcceptedToClientFrame(port);
		}
		return new PrivateMsgCnxRefusedToClientFrame();
	}

	@Override
	public void reset() {
		state = State.WAITING_STEP;
	}


}
