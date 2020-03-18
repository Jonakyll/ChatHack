package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogErrFrame implements Frame {
	
	private final String errMsg;
	
	public LogErrFrame(String errMsg) {
		this.errMsg = errMsg;
	}

	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(errMsg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put((byte) 4);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}

}
