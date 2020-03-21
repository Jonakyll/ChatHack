package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogResFromServerMDPFrame;

public class LogResFromMDPReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING,
		ERROR
	};
	
//	private final ByteBuffer bb;
	private State state = State.WAITING;
	private final byte opcode;
	private long id;
	
	private final LongReader idReader;
	
	public LogResFromMDPReader(byte opcode, ByteBuffer bb) {
		this.opcode = opcode;
//		this.bb = bb;
		this.idReader = new LongReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		ProcessStatus idStatus = idReader.process();
		if (idStatus != ProcessStatus.DONE) {
			return idStatus;
		}
		id = idReader.get();
		state = State.DONE;
		return ProcessStatus.DONE;
	}

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new LogResFromServerMDPFrame(opcode, id);
	}

	@Override
	public void reset() {
		idReader.reset();
		state = State.WAITING;
	}

}
