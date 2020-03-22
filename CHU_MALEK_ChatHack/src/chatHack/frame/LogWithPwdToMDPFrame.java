package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogWithPwdToMDPFrame implements Frame {

	private final String name;
	private final String password;
	
	public LogWithPwdToMDPFrame(String name, String password) {
		this.name = name;
		this.password = password;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogWithPwdToMDPFrame(this);
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

}
