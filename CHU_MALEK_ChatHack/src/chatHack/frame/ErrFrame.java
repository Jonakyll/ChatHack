package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ErrFrame implements Frame {
	
	private final String msg;
	
	public ErrFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put((byte) 4);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return "ERROR: " + msg;
	}

}
