package chatHack.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxAcceptedToClientFrame implements Frame {
	
	private final int port;
	private final long token;
	private final byte ipVersion;
	private final List<Byte> ip;
	private ByteBuffer ipBuff;
	
	public PrivateMsgCnxAcceptedToClientFrame(int port, long token, byte ipVersion, List<Byte> ip) {
		this.port = port;
		this.token = token;
		this.ipVersion = ipVersion;
		this.ip = ip;
	}

//	public ByteBuffer toByteBuffer() {
//		ipBuff = ByteBuffer.allocate(ipVersion * Byte.BYTES);
//		
//		ip.forEach(i -> ipBuff.put(i));
//		ipBuff.flip();
//		
//		ByteBuffer buff = ByteBuffer.allocate(4 * Byte.BYTES + Integer.BYTES + Long.BYTES + ipBuff.remaining());
//		
//		buff.put((byte) 2);
//		buff.put((byte) 1);
//		buff.put((byte) 0);
//		buff.putInt(port);
//		buff.putLong(token);
//		buff.put(ipVersion);
//		buff.put(ipBuff);
//		buff.flip();
//		
//		return buff;
//	}
	
	@Override
	public String toString() {
		ipBuff = ByteBuffer.allocate(ipVersion * Byte.BYTES);
		
		ip.forEach(i -> ipBuff.put(i));
		
		ipBuff.flip();
		StringJoiner joiner = new StringJoiner(".", "", "");

		while (ipBuff.hasRemaining()) {
			ByteBuffer tmp = ByteBuffer.allocate(Byte.BYTES).put(ipBuff.get());
			tmp.flip();
			joiner.add(StandardCharsets.UTF_8.decode(tmp));
		}
		
		return "you can now open a connection to " + joiner.toString() + " on port " + port;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxAcceptedToClientFrame(this);
	}
	
	public byte getIpVersion() {
		return ipVersion;
	}
	
	public List<Byte> getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public long getToken() {
		return token;
	}
	
}
