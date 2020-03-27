package chatHack.reader;

import chatHack.frame.Frame;
import chatHack.frame.LogOutFrame;

public class LogOutToServerReader implements Reader<Frame> {

	@Override
	public ProcessStatus process() {
		return ProcessStatus.DONE;
	}

	@Override
	public Frame get() {
		return new LogOutFrame("you have been disconnected from the public server");

	}

	@Override
	public void reset() {
	}

}
