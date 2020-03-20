package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogWithPwdToMDPFrame implements Frame {

	private final String name;
	private final String password;
	
	public LogWithPwdToMDPFrame(String name, String password) {
		this.name = name;
		this.password = password;
	}
	
//	public ByteBuffer toByteBuffer() {
//		Random random = new Random();
//		
//		ByteBuffer nameBuff = StandardCharsets.UTF_8.encode(name);
//		ByteBuffer passwordBuff = StandardCharsets.UTF_8.encode(password);
//		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + nameBuff.remaining() + passwordBuff.remaining());
//		
//		buff.put((byte) 1);
//		buff.putLong(random.nextLong());
//		buff.putInt(nameBuff.remaining());
//		buff.put(nameBuff);
//		buff.putInt(passwordBuff.remaining());
//		buff.put(passwordBuff);
//		buff.flip();
//		
//		return buff;
//	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogWithPwdToMDPFrame(this);
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

}
