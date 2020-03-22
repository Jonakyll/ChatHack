package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class SimpleMsgFrame implements Frame {
	
	private final byte opcode;
	private final String msg;
	
	public SimpleMsgFrame(byte opcode, String msg) {
		this.opcode = opcode;
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitSimpleMsgFrame(this);
	}
	
	public String getMsg() {
		return msg;
	}
	
	public byte getOpcode() {
		return opcode;
	}

}
