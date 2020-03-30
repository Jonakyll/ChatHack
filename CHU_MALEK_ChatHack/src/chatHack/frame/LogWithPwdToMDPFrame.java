package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import chatHack.visitor.FrameVisitor;

public class LogWithPwdToMDPFrame implements Frame {

	private final String name;
	private final String password;
	private long id;

	public LogWithPwdToMDPFrame(String name, String password) {
		this.name = name;
		this.password = password;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogWithPwdToMDPFrame(this);
	}

	@Override
	public ByteBuffer getByteBuffer() {
		Random random = new Random();
		id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(password);
		ByteBuffer buff = ByteBuffer.allocate(
				Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());

		buff.put((byte) 1);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.putInt(passwordBuff.remaining());
		buff.put(passwordBuff);
		buff.flip();
		
		return buff;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
	
	public long getId() {
		return id;
	}

}
