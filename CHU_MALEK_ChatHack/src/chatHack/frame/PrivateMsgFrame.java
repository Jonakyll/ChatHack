package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgFrame implements Frame {
	
	private final long token;
	private final String msg;
	
	public PrivateMsgFrame(long token, String msg) {
		this.token = token;
		this.msg = msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsg(this);
	}
	
	@Override
	public String toString() {
		return msg;
	}
	
	public long getToken() {
		return token;
	}
	
	public String getMsg() {
		return msg;
	}

}
