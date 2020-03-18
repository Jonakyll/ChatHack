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
		ByteBuffer pseudoBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer mdpBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + 2 * Integer.BYTES + pseudoBuff.remaining() + mdpBuff.remaining());

		buff.put((byte) 0);
		buff.putInt(pseudoBuff.remaining());
		buff.put(pseudoBuff);
		buff.putInt(mdpBuff.remaining());
		buff.put(mdpBuff);
		buff.flip();
		
		return buff;
	}
}
