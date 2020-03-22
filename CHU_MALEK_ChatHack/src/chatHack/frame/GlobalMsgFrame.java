package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class GlobalMsgFrame implements Frame {

	private final String exp;
	private final String msg;
	
	public GlobalMsgFrame(String exp, String msg) {
		this.exp = exp;
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return exp + ": " + msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitGlobalMsgFrame(this);
	}
	
	public String getExp() {
		return exp;
	}
	
	public String getMsg() {
		return msg;
	}

}
