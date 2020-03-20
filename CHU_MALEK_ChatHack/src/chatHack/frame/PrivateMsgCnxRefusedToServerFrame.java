package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);
		
		buff.put((byte) 2);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();
		
		return buff;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
