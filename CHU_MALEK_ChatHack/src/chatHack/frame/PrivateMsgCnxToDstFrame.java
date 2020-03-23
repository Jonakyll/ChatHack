package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxToDstFrame implements Frame {
	
	private final byte step;
	private final String src;
	private final String dst;
	
	public PrivateMsgCnxToDstFrame(byte step, String src, String dst) {
		this.step = step;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public String toString() {
		return src + " wants to start a private conversation with you.\n0     = accept\nother = decline";
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxToDstFrame(this);
	}
	
	public String getSrc() {
		return src;
	}

	public String getDst() {
		return dst;
	}
	
	public byte getStep() {
		return step;
	}
}
