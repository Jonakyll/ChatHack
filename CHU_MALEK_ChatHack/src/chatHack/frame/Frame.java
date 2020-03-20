package chatHack.frame;

import chatHack.visitor.FrameVisitor;

public interface Frame {

	void accept(FrameVisitor visitor);

}
