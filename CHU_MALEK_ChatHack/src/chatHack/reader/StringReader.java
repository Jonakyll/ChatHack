package chatHack.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author CHU Jonathan
 * Objet de l'interface Reader qui permet d'obtenir une chaine de
 * caracteres a partir d'un ByteBuffer.
 */
public class StringReader implements Reader<String> {

	private enum State {
		DONE, WAITING_INT, WAITING_STRING, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_INT;
	private int size;
	private StringBuilder msg = new StringBuilder();

	/**
	 * Cree un objet de type StringReader.
	 * @param bb, le ByteBuffer a analyser.
	 */
	public StringReader(ByteBuffer bb) {
		this.bb = bb;
	}

	/**
	 * Lis le ByteBuffer et stocke un message sous forme de chaine
	 * de caracteres ainsi que sa taille.
	 */
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		switch (state) {

		case WAITING_INT: {
			if (bb.remaining() < Integer.BYTES) {
				return ProcessStatus.REFILL;
			}
			size = bb.getInt();
			state = State.WAITING_STRING;
		}

		case WAITING_STRING: {
			
			if (bb.remaining() < size) {
				size -= bb.remaining();
				msg.append(StandardCharsets.UTF_8.decode(bb).toString());
				return ProcessStatus.REFILL;
			}
			
			int oldLimit = bb.limit();
			bb.limit(bb.position() + size);
			msg.append(StandardCharsets.UTF_8.decode(bb).toString());
			bb.limit(oldLimit);
			state = State.DONE;
			return ProcessStatus.DONE;
		}

		default:
			throw new AssertionError();
		}
	}

	/**
	 * Renvoie la chaine de caracteres correspondant au message.
	 */
	@Override
	public String get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return msg.toString();
	}

	/**
	 * Reinitialise le Reader.
	 */
	@Override
	public void reset() {
		state = State.WAITING_INT;
	}

}
