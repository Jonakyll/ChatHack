package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class LogOutFrame implements Frame {

	private final byte logoutType;
	private final String msg;
	
	public LogOutFrame(byte logoutType, String msg) {
		this.logoutType = logoutType;
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put((byte) 3);
		buff.put(logoutType);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
