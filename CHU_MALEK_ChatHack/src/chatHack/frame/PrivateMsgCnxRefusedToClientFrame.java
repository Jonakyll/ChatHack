package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToClientFrame implements Frame {

	private final String errMsg;

	public PrivateMsgCnxRefusedToClientFrame(String errMsg) {
		this.errMsg = errMsg;
	}

//	public ByteBuffer toByteBuffer() {
//		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(errMsg);
//		ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Integer.BYTES + errMsgBuff.remaining());
//
//		buff.put((byte) 2);
//		buff.put((byte) 1);
//		buff.put((byte) 1);
//		buff.putInt(errMsgBuff.remaining());
//		buff.put(errMsgBuff);
//		buff.flip();
//
//		return buff;
//	}
	
	@Override
	public String toString() {
		return errMsg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToClientFrame(this);
	}
	
	public String getErrMsg() {
		return errMsg;
	}

}
