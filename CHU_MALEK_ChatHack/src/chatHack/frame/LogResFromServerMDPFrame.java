package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

public class LogResFromServerMDPFrame implements Frame {
	
	private final byte opcode;
	private final long id;
	
	public LogResFromServerMDPFrame(byte opcode, long id) {
		this.opcode = opcode;
		this.id = id;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogResFromServerMDPFrame(this);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);

		buff.put(opcode);
		buff.putLong(id);
		buff.flip();
		
		return buff;
	}
	
	public byte getOpcode() {
		return opcode;
	}
	
	public long getId() {
		return id;
	}

}
