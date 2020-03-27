package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogOutFrame implements Frame {

	private final String msg;
	
	public LogOutFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogOutFrame(this);
	}
	
	public String getMsg() {
		return msg;
	}

}
