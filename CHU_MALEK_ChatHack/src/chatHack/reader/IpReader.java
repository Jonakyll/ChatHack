package chatHack.reader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IpReader implements Reader<List<Byte>> {

	private enum State {
		DONE, WAITING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING;
	private int ipVersion;
	private List<Byte> ip = new ArrayList<>();

	public IpReader(ByteBuffer bb, int ipVersion) {
		this.ipVersion = ipVersion;
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();

		try {

			switch (ipVersion) {

			case 4:
				if (bb.remaining() < 4 * Byte.BYTES) {
					return ProcessStatus.REFILL;
				}
				for (int i = 0; i < 4; ++i) {
					ip.add(bb.get());
				}
				state = State.DONE;
				return ProcessStatus.DONE;

			case 6:
				if (bb.remaining() < 16 * Byte.BYTES) {
					return ProcessStatus.REFILL;
				}
				for (int i = 0; i < 16; ++i) {
					ip.add(bb.get());
				}
				state = State.DONE;
				return ProcessStatus.DONE;

			default:
				throw new AssertionError();
			}
		} finally {
			bb.compact();
		}
	}

	@Override
	public List<Byte> get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return ip;
	}

	@Override
	public void reset() {
		state = State.WAITING;
	}

}
