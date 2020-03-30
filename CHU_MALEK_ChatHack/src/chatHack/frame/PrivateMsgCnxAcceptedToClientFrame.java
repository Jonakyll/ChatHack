package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxAcceptedToClientFrame implements Frame {
	
	private final String src;
	private final String dst;
	private final int port;
	private final long token;
	private final String ip;
	
	public PrivateMsgCnxAcceptedToClientFrame(String src, String dst, int port, long token, String ip) {
		this.src = src;
		this.dst = dst;
		this.port = port;
		this.token = token;
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "you can now open a connection to " + dst + ": " + ip + " on port " + port;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxAcceptedToClientFrame(this);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		ByteBuffer srcBuff = StandardCharsets.UTF_8.encode(src);
		ByteBuffer dstBuff = StandardCharsets.UTF_8.encode(dst);
		ByteBuffer ipBuff = StandardCharsets.UTF_8.encode(ip);
		ByteBuffer buff = ByteBuffer
				.allocate(3 * Byte.BYTES + 4 * Integer.BYTES + Long.BYTES + srcBuff.remaining() + dstBuff.remaining() + ipBuff.remaining());

		buff.put((byte) 4);
		buff.put((byte) 1);
		buff.put((byte) 0);
		buff.putInt(srcBuff.remaining());
		buff.put(srcBuff);
		buff.putInt(dstBuff.remaining());
		buff.put(dstBuff);
		buff.putInt(port);
		buff.putLong(token);
		buff.putInt(ipBuff.remaining());
		buff.put(ipBuff);
		buff.flip();
		
		return buff;
	}
	
	public String getSrc() {
		return src;
	}
	
	public String getDst() {
		return dst;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public long getToken() {
		return token;
	}
}
