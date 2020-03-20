package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
//	public ByteBuffer toByteBuffer() {
//		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES);
//		
//		buff.put((byte) 2);
//		buff.put((byte) 1);
//		buff.put((byte) 1);
//		buff.flip();
//		
//		return buff;
//	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToServerFrame(this);
	}

}
