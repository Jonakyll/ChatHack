package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class LogNoPwdToMDPFrame implements Frame {

	private final String name;
	
	public LogNoPwdToMDPFrame(String name) {
		this.name = name;
	}

	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitLogNoPwdToMDPFrame(this);
	}
	
	public String getName() {
		return name;
	}

}
