package chatHack.reader;

import chatHack.frame.Frame;

public interface Reader {

	public static enum ProcessStatus {
		DONE,
		REFILL,
		ERROR
	};
	
	ProcessStatus process();
	
	Frame get();
	
	void reset();
	
}
