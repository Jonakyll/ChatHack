package chatHack.frame;

import java.nio.ByteBuffer;

public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);
		
		buff.put((byte) 2);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();
		
		return buff;
	}

}