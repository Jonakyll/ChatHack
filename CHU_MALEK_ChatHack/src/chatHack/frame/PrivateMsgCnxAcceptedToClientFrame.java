package chatHack.frame;

import java.nio.ByteBuffer;

public class PrivateMsgCnxAcceptedToClientFrame implements Frame {
	
	private final int port;
	
	public PrivateMsgCnxAcceptedToClientFrame(int port) {
		this.port = port;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Integer.BYTES);
		
		buff.put((byte) 2);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(port);
		buff.flip();
		
		return buff;
	}

}
