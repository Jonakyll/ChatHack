package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxToDstFrame implements Frame {
	
	private final byte step;
	private final String dst;
	
	public PrivateMsgCnxToDstFrame(byte step, String dst) {
		this.step = step;
		this.dst = dst;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer buff = ByteBuffer.allocate(2 * Byte.BYTES + Integer.BYTES + dstBuff.remaining());
		
		buff.put((byte) 2);
		buff.put(step);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return dst + " wants to open a private channel with you.";
	}

	@Override
	public void accept(FrameVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
