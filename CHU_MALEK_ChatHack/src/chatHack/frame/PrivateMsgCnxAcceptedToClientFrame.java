package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxAcceptedToClientFrame implements Frame {
	
	private final String src;
	private final int port;
	private final long token;
	private final String ip;
	
	public PrivateMsgCnxAcceptedToClientFrame(String src, int port, long token, String ip) {
		this.src = src;
		this.port = port;
		this.token = token;
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "you can now open a connection to " + ip + " on port " + port;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxAcceptedToClientFrame(this);
	}
	
	public String getSrc() {
		return src;
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
