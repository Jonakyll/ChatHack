package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToClientFrame implements Frame {

	private final String src;
	private final String dst;
	private final String errMsg;

	public PrivateMsgCnxRefusedToClientFrame(String src, String dst, String errMsg) {
		this.src = src;
		this.dst = dst;
		this.errMsg = errMsg;
	}

	@Override
	public String toString() {
		return errMsg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToClientFrame(this);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer errMsgBuff = StandardCharsets.UTF_8.encode(errMsg);
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 3 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining() + errMsgBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 1);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.putInt(errMsgBuff.remaining());
		buff.put(errMsgBuff);
		buff.flip();
		
		return buff;
	}
	
	public String getSrc() {
		return src;
	}
	
	public String getDst() {
		return dst;
	}
	
	public String getErrMsg() {
		return errMsg;
	}

}
