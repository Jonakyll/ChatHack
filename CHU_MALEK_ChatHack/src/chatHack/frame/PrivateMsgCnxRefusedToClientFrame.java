package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToClientFrame implements Frame {

	private final String src;
	private final String dst;
	private final String errMsg;

	public PrivateMsgCnxRefusedToClientFrame(String src, String dst, String errMsg) {
		this.src = src;
		this.dst = dst;
		this.errMsg = errMsg;
	}

	@Override
	public String toString() {
		return errMsg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToClientFrame(this);
	}
	
	public String getSrc() {
		return src;
	}
	
	public String getDst() {
		return dst;
	}
	
	public String getErrMsg() {
		return errMsg;
	}

}
