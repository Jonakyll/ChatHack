package chatHack.frame;

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
