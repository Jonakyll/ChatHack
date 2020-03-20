package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

public interface Frame {

	ByteBuffer toByteBuffer();
	
	void accept(FrameVisitor visitor);

}
