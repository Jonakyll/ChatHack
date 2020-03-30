package chatHack.frame;

import java.nio.ByteBuffer;

import chatHack.visitor.FrameVisitor;

public interface Frame {

	void accept(FrameVisitor visitor);
	
	ByteBuffer getByteBuffer();

}
