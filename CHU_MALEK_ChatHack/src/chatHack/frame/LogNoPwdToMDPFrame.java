package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogNoPwdToMDPFrame implements Frame {

	private final String name;
	
	public LogNoPwdToMDPFrame(String name) {
		this.name = name;
	}
	
//	public ByteBuffer toByteBuffer() {
//		Random random = new Random();
//		
//		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
//		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + nameBuff.remaining());
//		
//		buff.put((byte) 1);
//		buff.putLong(random.nextLong());
//		buff.putInt(nameBuff.remaining());
//		buff.put(nameBuff);
//		buff.flip();
//		
//		return buff;
//	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogNoPwdToMDPFrame(this);
	}
	
	public String getName() {
		return name;
	}

}
