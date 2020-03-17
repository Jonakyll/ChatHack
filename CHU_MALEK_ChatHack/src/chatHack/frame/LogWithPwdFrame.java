package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogWithPwdFrame implements Frame {
	
	private final String name;
	private final String password;

	public LogWithPwdFrame(String name, String password) {
		this.name = name;
		this.password = password;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(password);
		ByteBuffer buff = ByteBuffer.allocate(2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());
		
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.putInt(passwordBuff.remaining());
		buff.put(passwordBuff);
		buff.flip();
		
		return buff;
	}

}
