package chatHack.reader;

import java.nio.ByteBuffer;
import java.util.List;

import chatHack.frame.Frame;
import chatHack.frame.LogOutFrame;

public class LogOutToServerReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_TYPE, WAITING_IP_VERSION, WAITING_IP, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_TYPE;
	private byte logOutType;
	private byte ipVersion;
	private List<Byte> ip;

	private final ByteReader logOUtTypeReader;
	private final ByteReader ipVersionReader;
	private IpReader ipReader;

	public LogOutToServerReader(ByteBuffer bb) {
		this.bb = bb;
		this.logOUtTypeReader = new ByteReader(bb);
		this.ipVersionReader = new ByteReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_TYPE:
			ProcessStatus logOutTypeStatus = logOUtTypeReader.process();
			if (logOutTypeStatus != ProcessStatus.DONE) {
				return logOutTypeStatus;
			}
			logOutType = logOUtTypeReader.get();
			if (logOutType == 0) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			state = State.WAITING_IP_VERSION;

		case WAITING_IP_VERSION:
			ProcessStatus ipVersionStatus = ipVersionReader.process();
			if (ipVersionStatus != ProcessStatus.DONE) {
				return ipVersionStatus;
			}
			ipVersion = ipVersionReader.get();
			if (ipVersion == 4) {
				ipReader = new IpReader(bb, 4);
			} else {
				ipReader = new IpReader(bb, 6);
			}
			state = State.WAITING_IP;

		case WAITING_IP:
			ProcessStatus ipStatus = ipReader.process();
			if (ipStatus != ProcessStatus.DONE) {
				return ipStatus;
			}
			ip = ipReader.get();
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
			return new LogOutFrame((byte) 0, "you have been disconnected from the public server");
		}
		return new LogOutFrame((byte) 1, "you have been disconnected from a private connection with " + ip);
	}

	@Override
	public void reset() {
		logOUtTypeReader.reset();
		ipVersionReader.reset();

		if (ipReader != null) {
			ipReader.reset();
		}
		state = State.WAITING_TYPE;
	}

}
