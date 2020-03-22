package chatHack.reader;

import java.nio.ByteBuffer;
import java.util.List;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;

public class PrivateMsgCnxResToServerReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_RES_TYPE, WAITING_PORT, WAITING_TOKEN, WAITING_IP_VERSION, WAITING_IP, ERROR,
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_RES_TYPE;
	private byte resType;
	private int port;
	private long token;
	private byte ipVersion;
	private List<Byte> ip;

	private final ByteReader resTypeReader;
	private final IntReader portReader;
	private final LongReader tokenReader;
	private final ByteReader ipVersionReader;
	private IpReader ipReader;

	public PrivateMsgCnxResToServerReader(ByteBuffer bb) {
		this.bb = bb;
		this.resTypeReader = new ByteReader(bb);
		this.portReader = new IntReader(bb);
		this.tokenReader = new LongReader(bb);
		this.ipVersionReader = new ByteReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_RES_TYPE:
			ProcessStatus resTypeStatus = resTypeReader.process();
			if (resTypeStatus != ProcessStatus.DONE) {
				return resTypeStatus;
			}
			resType = resTypeReader.get();
			if (resType == 1) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			state = State.WAITING_PORT;
			// tester le cas d'erreur

		case WAITING_PORT:
			ProcessStatus portStatus = portReader.process();
			if (portStatus != ProcessStatus.DONE) {
				return portStatus;
			}
			port = portReader.get();
			state = State.WAITING_TOKEN;

		case WAITING_TOKEN:
			ProcessStatus tokenStatus = tokenReader.process();
			if (tokenStatus != ProcessStatus.DONE) {
				return tokenStatus;
			}
			token = tokenReader.get();
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

		if (resType == 0) {
			return new PrivateMsgCnxAcceptedToClientFrame(port, token, ipVersion, ip);
		}
		return new PrivateMsgCnxRefusedToClientFrame("cnx to private channel refused");
	}

	@Override
	public void reset() {
		resTypeReader.reset();
		portReader.reset();
		tokenReader.reset();
		ipVersionReader.reset();
		state = State.WAITING_RES_TYPE;
	}

}
