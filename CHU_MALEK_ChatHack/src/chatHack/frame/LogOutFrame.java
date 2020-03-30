package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class LogOutFrame implements Frame {

	private final String msg;
	
	public LogOutFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogOutFrame(this);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());

		buff.put((byte) 5);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	public String getMsg() {
		return msg;
	}


}
