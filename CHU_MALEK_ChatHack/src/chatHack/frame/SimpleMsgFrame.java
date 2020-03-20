package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SimpleMsgFrame implements Frame {
	
	private final byte opcode;
	private final String msg;
	
	public SimpleMsgFrame(byte opcode, String msg) {
		this.opcode = opcode;
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put(opcode);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return msg;
	}

}
