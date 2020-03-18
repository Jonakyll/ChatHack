package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageFrame implements Frame {

	private final String msg;
	
	public MessageFrame(String msg) {
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Integer.BYTES + msgBuff.remaining());
		
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	public String getMsg() {
		return msg;
	}

}
