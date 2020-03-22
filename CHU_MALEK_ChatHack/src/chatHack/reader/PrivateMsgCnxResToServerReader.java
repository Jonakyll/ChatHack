package chatHack.reader;

import java.nio.ByteBuffer;
import java.util.List;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;

public class PrivateMsgCnxResToServerReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_RES_TYPE, WAITING_SRC, WAITING_PORT, WAITING_TOKEN, WAITING_IP, ERROR,
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_RES_TYPE;
	private byte resType;
	private String src;
	private int port;
	private long token;
	private String ip;
	
	private final ByteReader resTypeReader;
	private final StringReader srcReader;
	private final IntReader portReader;
	private final LongReader tokenReader;
	private final StringReader ipReader;
	
	public PrivateMsgCnxResToServerReader(ByteBuffer bb) {
		this.bb = bb;
		this.resTypeReader = new ByteReader(bb);
		this.srcReader = new StringReader(bb);
		this.portReader = new IntReader(bb);
		this.tokenReader = new LongReader(bb);
		this.ipReader = new StringReader(bb);
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
			state = State.WAITING_SRC;
			
		case WAITING_SRC:
			ProcessStatus srcStatus = srcReader.process();
			if (srcStatus != ProcessStatus.DONE) {
				return srcStatus;
			}
			src = srcReader.get();
			if (resType == 1) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			state = State.WAITING_PORT;

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
			return new PrivateMsgCnxAcceptedToClientFrame(src, port, token, ip);
		}
		return new PrivateMsgCnxRefusedToClientFrame(src, "cnx to private channel refused");
	}

	@Override
	public void reset() {
		resTypeReader.reset();
		srcReader.reset();
		portReader.reset();
		tokenReader.reset();
		ipReader.reset();
		state = State.WAITING_RES_TYPE;
	}

}
