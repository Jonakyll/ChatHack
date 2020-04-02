package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgFrame implements Frame {

	private final byte type;
	private final long token;
	private final String src;
	private String fileName;
	private final String msg;

	public PrivateMsgFrame(byte type, long token, String src, String msg) {
		this.type = type;
		this.token = token;
		this.src = src;
		this.msg = msg;
	}

	public PrivateMsgFrame(byte type, long token, String src, String fileName, String msg) {
		this.type = type;
		this.token = token;
		this.src = src;
		this.fileName = fileName;
		this.msg = msg;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsg(this);
	}

	@Override
	public ByteBuffer getByteBuffer() {
		if (type == 0) {
			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
			ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
			ByteBuffer buff = ByteBuffer.allocate(
					3 * Byte.BYTES + Long.BYTES + 2 * Integer.BYTES + srcBuff.remaining() + msgBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 2);
			buff.put(type);
			buff.putLong(token);
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(msgBuff.remaining());
			buff.put(msgBuff);
			buff.flip();

			return buff;
		} else {
			ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
			ByteBuffer fileNameBuff = StandardCharsets.UTF_8.encode(fileName);
			ByteBuffer msgBuff = StandardCharsets.UTF_8.encode(msg);
			ByteBuffer buff = ByteBuffer.allocate(3 * Byte.BYTES + Long.BYTES + 3 * Integer.BYTES + srcBuff.remaining()
					+ fileNameBuff.remaining() + msgBuff.remaining());

			buff.put((byte) 4);
			buff.put((byte) 2);
			buff.put(type);
			buff.putLong(token);
			buff.putInt(srcBuff.remaining());
			buff.put(srcBuff);
			buff.putInt(fileNameBuff.remaining());
			buff.put(fileNameBuff);
			buff.putInt(msgBuff.remaining());
			buff.put(msgBuff);
			buff.flip();

			return buff;
		}
	}

	@Override
	public String toString() {
		return src + ": " + msg;
	}

	public byte getType() {
		return type;
	}

	public long getToken() {
		return token;
	}

	public String getSrc() {
		return src;
	}

	public String getFileName() {
		return fileName;
	}

	public String getMsg() {
		return msg;
	}

}
