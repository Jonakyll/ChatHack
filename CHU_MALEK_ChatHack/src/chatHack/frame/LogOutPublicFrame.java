package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogOutPublicFrame implements Frame {

	private final String msg;
	
	public LogOutPublicFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put((byte) 3);
		buff.put((byte) 0);
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
