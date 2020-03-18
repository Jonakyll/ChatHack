package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogErrFrame;
import chatHack.frame.LogNoPwdFrame;
import chatHack.frame.LogWithPwdFrame;
import chatHack.frame.MessageFrame;
import chatHack.frame.TestFrame;

public class LogReader implements Reader<Frame> {
	
	private enum State {
		DONE,
		WAITING_CODE,
		WAITING_NAME,
		WAITING_PASSWORD,
		ERROR
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
			state = State.WAITING_PASSWORD;
			
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
		
		return new LogErrFrame("log error");

//		switch (code) {
//		case 0:
//			return new LogWithPwdFrame(name, password);
//		case 1:
//			return new LogNoPwdFrame(name);
//		default:
//			return new LogErrFrame();
//		}
	} 

	@Override
	public void reset() {
		codeReader.reset();
		nameReader.reset();
		passwordReader.reset();
		state = State.WAITING_CODE;
	}

}
