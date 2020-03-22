package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogOutFrame implements Frame {

	private final byte logoutType;
	private final String msg;
	
	public LogOutFrame(byte logoutType, String msg) {
		this.logoutType = logoutType;
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
	
	public byte getLogoutType() {
		return logoutType;
	}
	
	public String getMsg() {
		return msg;
	}

}
