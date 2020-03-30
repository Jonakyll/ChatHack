package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxToDstFrame implements Frame {

	private final byte step;
	private final String src;
	private final String dst;

	public PrivateMsgCnxToDstFrame(byte step, String src, String dst) {
		this.step = step;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public String toString() {
		return src + " wants to start a private conversation with you.\n\n0     = accept\nother = decline";
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxToDstFrame(this);
	}

	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer buff = ByteBuffer
				.allocate(2 * Byte.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + dstBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.flip();
		
		return buff;
	}

	public String getSrc() {
		return src;
	}

	public String getDst() {
		return dst;
	}

	public byte getStep() {
		return step;
	}
}
