package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class GlobalMsgFrame implements Frame {

	private final String exp;
	private final String msg;
	
	public GlobalMsgFrame(String exp, String msg) {
		this.exp = exp;
		this.msg = msg;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer expBuff = StandardCharsets.UTF_8.encode(exp);
		ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(Byte.BYTES + 2 * Integer.BYTES + expBuff.remaining() + msgBuff.remaining());
		
		buff.put((byte) 1);
		buff.putInt(expBuff.remaining());
		buff.put(expBuff);
		buff.putInt(msgBuff.remaining());
		buff.put(msgBuff);
		buff.flip();
		
		return buff;
	}
	
	@Override
	public String toString() {
		return exp + ": " + msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
