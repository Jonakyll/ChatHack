package chatHack.frame;

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
