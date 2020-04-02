package chatHack.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<String> {

	private enum State {
		DONE, WAITING_INT, WAITING_STRING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_INT;
	private int size;
	private StringBuilder msg = new StringBuilder();

	public StringReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_INT: {
			if (bb.remaining() < Integer.BYTES) {
				return ProcessStatus.REFILL;
			}
			size = bb.getInt();
			state = State.WAITING_STRING;
		}

		case WAITING_STRING: {
			
//			le pb est ici
			
			if (bb.remaining() < size) {
				size -= bb.remaining();
				msg.append(StandardCharsets.UTF_8.decode(bb).toString());
				return ProcessStatus.REFILL;
			}
			int oldLimit = bb.limit();
			bb.limit(bb.position() + size);
			msg.append(StandardCharsets.UTF_8.decode(bb).toString());
			bb.limit(oldLimit);
			state = State.DONE;
			return ProcessStatus.DONE;
		}

		default:
			throw new AssertionError();
		}
	}

	@Override
	public String get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return msg.toString();
	}

	@Override
	public void reset() {
		state = State.WAITING_INT;
	}

}
