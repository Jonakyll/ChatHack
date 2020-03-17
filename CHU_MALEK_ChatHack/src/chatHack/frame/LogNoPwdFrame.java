package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogNoPwdFrame implements Frame {
	
	private final String name;

	public LogNoPwdFrame(String name) {
		this.name = name;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer buff = ByteBuffer.allocate(2 * Integer.BYTES + nameBuff.remaining());
		
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.flip();
		
		return buff;
	}

}
