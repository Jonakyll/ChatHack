package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.SimpleMsgFrame;

public class LogReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_CODE, WAITING_NAME, WAITING_PASSWORD, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_CODE;
	private byte code;
	private String name;
	private String password;

	private final ByteReader codeReader;
	private final StringReader nameReader;
	private final StringReader passwordReader;

	public LogReader(ByteBuffer bb) {
		this.bb = bb;
		this.codeReader = new ByteReader(bb);
		this.nameReader = new StringReader(bb);
		this.passwordReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {
		case WAITING_CODE: {
			ProcessStatus codeStatus = codeReader.process();
			if (codeStatus != ProcessStatus.DONE) {
				return codeStatus;
			}
			code = codeReader.get();
			state = State.WAITING_NAME;
		}

		case WAITING_NAME: {
			ProcessStatus nameStatus = nameReader.process();
			if (nameStatus != ProcessStatus.DONE) {
				return nameStatus;
			}
			name = nameReader.get();

			if (code == 0) {
				state = State.WAITING_PASSWORD;
			} else if (code == 1) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			// tester le cas d'erreur
		}

		case WAITING_PASSWORD: {
			ProcessStatus passwordStatus = passwordReader.process();
			if (passwordStatus != ProcessStatus.DONE) {
				return passwordStatus;
			}
			password = passwordReader.get();
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

//		return new SimpleMsgFrame((byte) 0, "you are connected as " + name);

//		les frames a envoyer au serverMDP
		if (code == 0) {
			return new LogWithPwdToMDPFrame(name, password);
		}
		return new LogNoPwdToMDPFrame(name);
	}

	@Override
	public void reset() {
		codeReader.reset();
		nameReader.reset();
		passwordReader.reset();
		state = State.WAITING_CODE;
	}

}
