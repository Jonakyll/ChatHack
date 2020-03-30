package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import chatHack.visitor.FrameVisitor;

public class LogNoPwdToMDPFrame implements Frame {

	private final String name;
	private long id;
	
	public LogNoPwdToMDPFrame(String name) {
		this.name = name;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogNoPwdToMDPFrame(this);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		Random random = new Random();
		id = random.nextLong();

		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + nameBuff.remaining());

		buff.put((byte) 2);
		buff.putLong(id);
		buff.putInt(nameBuff.remaining());
		buff.put(nameBuff);
		buff.flip();
		
		return buff;
	}
	
	public String getName() {
		return name;
	}
	
	public long getId() {
		return id;
	}


}
