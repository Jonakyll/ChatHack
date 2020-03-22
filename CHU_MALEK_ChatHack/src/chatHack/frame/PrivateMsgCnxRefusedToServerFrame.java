package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public class PrivateMsgCnxRefusedToServerFrame implements Frame {

	public PrivateMsgCnxRefusedToServerFrame() {
	}
	
	@Override
	public void accept(FrameVisitor visitor) {
		visitor.visitPrivateMsgCnxRefusedToServerFrame(this);
	}

}
