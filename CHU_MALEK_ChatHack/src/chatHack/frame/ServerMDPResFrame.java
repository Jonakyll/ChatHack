package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class ServerMDPResFrame implements Frame {
	
	private final byte opcode;
	private final long id;
	
	public ServerMDPResFrame(byte opcode, long id) {
		this.opcode = opcode;
		this.id = id;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitServerMDPResFrame(this);
	}
	
	public byte getOpcode() {
		return opcode;
	}
	
	public long getId() {
		return id;
	}

}
