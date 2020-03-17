package chatHack.frame;

import java.nio.ByteBuffer;

public class TestFrame implements Frame {
	
	private byte b;
	
	public TestFrame(byte b) {
		this.b = b;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES);
		
		buff.put(b);
		buff.flip();
		
		return buff;
	}

}
