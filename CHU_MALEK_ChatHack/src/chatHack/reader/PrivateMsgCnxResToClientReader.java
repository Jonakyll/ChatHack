package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;

public class PrivateMsgCnxResToClientReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_RES_TYPE, WAITING_SRC, WAITING_DST, WAITING_PORT, WAITING_TOKEN, WAITING_IP, WAITING_ERR_MSG, ERROR
	};

//	private final ByteBuffer bb;
	private State state = State.WAITING_RES_TYPE;

	private byte resType;
	private String src;
	private String dst;
	private int port;
	private long token;
	private String ip;

	private String errMsg;

	private final ByteReader resTypeReader;
	private final StringReader srcReader;
	private final StringReader dstReader;
	private final IntReader portReader;
	private final LongReader tokenReader;
	private final StringReader ipReader;

	private final StringReader errMsgReader;

	public PrivateMsgCnxResToClientReader(ByteBuffer bb) {
//		this.bb = bb;
		this.resTypeReader = new ByteReader(bb);
		this.srcReader = new StringReader(bb);
		this.dstReader = new StringReader(bb);
		this.portReader = new IntReader(bb);
		this.tokenReader = new LongReader(bb);
		this.ipReader = new StringReader(bb);

		this.errMsgReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {

			switch (state) {

			case WAITING_RES_TYPE:
				ProcessStatus resTypeStatus = resTypeReader.process();
				if (resTypeStatus != ProcessStatus.DONE) {
					return resTypeStatus;
				}
				resType = resTypeReader.get();
				if (resType == 1) {
					state = State.WAITING_ERR_MSG;
					break;
				} else {
					state = State.WAITING_SRC;
					break;
				}
				// tester le cas d'erreur

			case WAITING_SRC:
				ProcessStatus srcStatus = srcReader.process();
				if (srcStatus != ProcessStatus.DONE) {
					return srcStatus;
				}
				src = srcReader.get();
				state = State.WAITING_DST;
				break;

			case WAITING_DST:
				ProcessStatus dstStatus = dstReader.process();
				if (dstStatus != ProcessStatus.DONE) {
					return dstStatus;
				}
				dst = dstReader.get();
				state = State.WAITING_PORT;
				break;

			case WAITING_PORT:
				ProcessStatus portStatus = portReader.process();
				if (portStatus != ProcessStatus.DONE) {
					return portStatus;
				}
				port = portReader.get();
				state = State.WAITING_TOKEN;
				break;

			case WAITING_TOKEN:
				ProcessStatus tokenStatus = tokenReader.process();
				if (tokenStatus != ProcessStatus.DONE) {
					return tokenStatus;
				}
				token = tokenReader.get();
				state = State.WAITING_IP;
				break;

			case WAITING_IP:
				ProcessStatus ipStatus = ipReader.process();
				if (ipStatus != ProcessStatus.DONE) {
					return ipStatus;
				}
				ip = ipReader.get();
				state = State.DONE;
				return ProcessStatus.DONE;

			case WAITING_ERR_MSG:
				ProcessStatus errMsgStatus = errMsgReader.process();
				if (errMsgStatus != ProcessStatus.DONE) {
					return errMsgStatus;
				}
				errMsg = errMsgReader.get();
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

		if (resType == 0) {
			return new PrivateMsgCnxAcceptedToClientFrame(src, dst, port, token, ip);

		}
		return new PrivateMsgCnxRefusedToClientFrame(src, dst, errMsg);
	}

	@Override
	public void reset() {
		resTypeReader.reset();
		srcReader.reset();
		dstReader.reset();
		portReader.reset();
		tokenReader.reset();
		ipReader.reset();
		errMsgReader.reset();
		state = State.WAITING_RES_TYPE;
	}

}
