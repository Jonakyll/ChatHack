package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToClientFrame implements Frame {

	private final String errMsg;

	public PrivateMsgCnxRefusedToClientFrame(String errMsg) {
		this.errMsg = errMsg;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(errMsg);
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Integer.BYTES + errMsgBuff.remaining());

		buff.put((byte) 2);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.putInt(errMsgBuff.remaining());
		buff.put(errMsgBuff);
		buff.flip();

		return buff;
	}
	
	@Override
	public String toString() {
		return errMsg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
