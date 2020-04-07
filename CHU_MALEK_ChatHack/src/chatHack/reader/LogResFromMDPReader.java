package chatHack.reader;

import java.nio.ByteBuffer;

import chatHack.frame.Frame;
import chatHack.frame.LogResFromServerMDPFrame;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir un objet Frame
 * reponse venant du serveur MDP a partir d'un ByteBuffer.
 */
public class LogResFromMDPReader implements Reader<Frame> {

	private enum State {
		DONE,
		WAITING,
		ERROR
	};
	
	private State state = State.WAITING;
	private final byte opcode;
	private long id;
	
	private final LongReader idReader;

	/**
	 * Cree un objet de type LogResFromMDPReader.
	 * @param opcode, le type de reponse du serveur MDP.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public LogResFromMDPReader(byte opcode, ByteBuffer bb) {
		this.opcode = opcode;
		this.idReader = new LongReader(bb);
	}
	
	/**
	 * Lis le ByteBuffer et stocke les informations liees a la reponse du serveur MDP.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		ProcessStatus idStatus = idReader.process();
		if (idStatus != ProcessStatus.DONE) {
			return idStatus;
		}
		id = idReader.get();
		state = State.DONE;
		return ProcessStatus.DONE;
	}

	/**
	 * Renvoie une Frame de reponse du serveur MDP.
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new LogResFromServerMDPFrame(opcode, id);
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		idReader.reset();
		state = State.WAITING;
	}

}
