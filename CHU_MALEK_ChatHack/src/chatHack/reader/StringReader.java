package chatHack.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<String> {

	private enum State {
		DONE,
		WAITING_INT,
		WAITING_STRING,
		ERROR
	};
	
	private final ByteBuffer bb;
	private State state = State.WAITING_INT;
	private int size;
	private String msg;
	
	private final IntReader intReader;
	
	public StringReader(ByteBuffer bb) {
		this.bb = bb;
		this.intReader = new IntReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();
		
		try {
			switch (state) {
			
			case WAITING_INT: {
				ProcessStatus intStatus = intReader.process();
				if (intStatus != ProcessStatus.DONE) {
					return intStatus;
				}
				size = intReader.get();
				state = State.WAITING_STRING;
			}
			
			case WAITING_STRING: {
				if (bb.remaining() < size) {
					return ProcessStatus.REFILL;
				}
				int oldLimit = bb.limit();
				msg = StandardCharsets.UTF_8.decode(bb).toString();
				bb.limit(oldLimit);
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			
			default: 
				throw new AssertionError();
			}
		} finally {
			bb.compact();
		}
	}

	@Override
	public String get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return msg;
	}

	@Override
	public void reset() {
		state = State.WAITING_INT;
	}

}
