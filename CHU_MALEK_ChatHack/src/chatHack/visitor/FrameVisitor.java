package chatHack.visitor;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.GlobalMsgFrame;
import chatHack.frame.LogNoPwdToMDPFrame;
import chatHack.frame.LogOutFrame;
import chatHack.frame.LogWithPwdToMDPFrame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToServerFrame;
import chatHack.frame.PrivateMsgCnxToDstFrame;
import chatHack.frame.LogResFromServerMDPFrame;
import chatHack.frame.SimpleMsgFrame;

public interface FrameVisitor {

//	Frame visit();
	
	ByteBuffer visitGlobalMsgFrame(GlobalMsgFrame frame);
	
	ByteBuffer visitLogNoPwdToMDPFrame(LogNoPwdToMDPFrame frame);
	
	ByteBuffer visitLogOutFrame(LogOutFrame frame);
	
	ByteBuffer visitLogWithPwdToMDPFrame(LogWithPwdToMDPFrame frame);
	
	ByteBuffer visitPrivateMsgCnxAcceptedToClientFrame(PrivateMsgCnxAcceptedToClientFrame frame);
	
	ByteBuffer visitPrivateMsgCnxRefusedToClientFrame(PrivateMsgCnxRefusedToClientFrame frame);
	
	ByteBuffer visitPrivateMsgCnxRefusedToServerFrame(PrivateMsgCnxRefusedToServerFrame frame);
	
	ByteBuffer visitPrivateMsgCnxToDstFrame(PrivateMsgCnxToDstFrame frame);
	
	ByteBuffer visitSimpleMsgFrame(SimpleMsgFrame frame);

	ByteBuffer visitLogResFromServerMDPFrame(LogResFromServerMDPFrame frame);
}
