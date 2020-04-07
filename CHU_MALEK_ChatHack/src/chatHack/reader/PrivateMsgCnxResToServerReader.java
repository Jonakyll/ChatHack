package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.PrivateMsgCnxAcceptedToClientFrame;
import chatHack.frame.PrivateMsgCnxRefusedToClientFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un objet Frame
 * de reponse a une demande de connexion privee a envoyer a un client ChatHack a partir d'un ByteBuffer.
 */
public class PrivateMsgCnxResToServerReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_RES_TYPE, WAITING_SRC, WAITING_DST, WAITING_PORT, WAITING_TOKEN, WAITING_IP, ERROR,
	};

	private State state = State.WAITING_RES_TYPE;
	private byte resType;
	private String src;
	private String dst;
	private int port;
	private long token;
	private String ip;
	
	private final ByteReader resTypeReader;
	private final StringReader srcReader;
	private final StringReader dstReader;
	private final IntReader portReader;
	private final LongReader tokenReader;
	private final StringReader ipReader;

	/**
	 * Cree un objet de type PrivateMsgCnxResToServerReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public PrivateMsgCnxResToServerReader(ByteBuffer bb) {
		this.resTypeReader = new ByteReader(bb);
		this.srcReader = new StringReader(bb);
		this.dstReader = new StringReader(bb);
		this.portReader = new IntReader(bb);
		this.tokenReader = new LongReader(bb);
		this.ipReader = new StringReader(bb);
	}

	/**
	 * Lis le ByteBuffer et stocke les informations liees
	 * a une reponse de demande de connexion privee.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_RES_TYPE:
			ProcessStatus resTypeStatus = resTypeReader.process();
			if (resTypeStatus != ProcessStatus.DONE) {
				return resTypeStatus;
			}
			resType = resTypeReader.get();
			state = State.WAITING_SRC;
			
		case WAITING_SRC:
			ProcessStatus srcStatus = srcReader.process();
			if (srcStatus != ProcessStatus.DONE) {
				return srcStatus;
			}
			src = srcReader.get();
			if (resType == 1) {
				state = State.DONE;
				return ProcessStatus.DONE;
			}
			state = State.WAITING_DST;
			
		case WAITING_DST:
			ProcessStatus dstStatus = dstReader.process();
			if (dstStatus != ProcessStatus.DONE) {
				return dstStatus;
			}
			dst = dstReader.get();
			state = State.WAITING_PORT;

		case WAITING_PORT:
			ProcessStatus portStatus = portReader.process();
			if (portStatus != ProcessStatus.DONE) {
				return portStatus;
			}
			port = portReader.get();
			state = State.WAITING_TOKEN;

		case WAITING_TOKEN:
			ProcessStatus tokenStatus = tokenReader.process();
			if (tokenStatus != ProcessStatus.DONE) {
				return tokenStatus;
			}
			token = tokenReader.get();
			state = State.WAITING_IP;

		case WAITING_IP:
			ProcessStatus ipStatus = ipReader.process();
			if (ipStatus != ProcessStatus.DONE) {
				return ipStatus;
			}
			ip = ipReader.get();
			state = State.DONE;
			return ProcessStatus.DONE;

		default:
			throw new AssertionError();
		}
	}

	/**
	 * Renvoie une Frame de reponse a une demande de connexion privee.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}

		if (resType == 0) {
			return new PrivateMsgCnxAcceptedToClientFrame(src, dst, port, token, ip);
		}
		return new PrivateMsgCnxRefusedToClientFrame(src, dst, "cnx to private channel refused");
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		resTypeReader.reset();
		srcReader.reset();
		dstReader.reset();
		portReader.reset();
		tokenReader.reset();
		ipReader.reset();
		state = State.WAITING_RES_TYPE;
	}

}
