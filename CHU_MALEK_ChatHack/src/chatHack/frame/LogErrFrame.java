package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogErrFrame implements Frame {

	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode("log error");
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + msgBuff.remaining());
		
		buff.put((byte) 4);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}

}
