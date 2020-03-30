package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToServerFrame(this);
	}

	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.flip();
		
		return buff;
	}

}
