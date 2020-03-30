package chatHack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface Context {

	void processIn();
	
	void queueFrame(ByteBuffer buff);
	
	void processOut();
	
	void updateInterestOps();
	
	void silentlyClose();
	
	void doConnect() throws IOException;
	
	void doWrite() throws IOException;
	
	void doRead() throws IOException;
	
	SelectionKey getKey();
	
}
