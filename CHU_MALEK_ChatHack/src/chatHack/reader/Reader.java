package chatHack.reader;

/**
 * 
 * @author CHU Jonathan
 * Interface qui sert a analyser une frame sous forme de ByteBuffer
 * et d'en recuperer des informations.
 * @param <E> type de retour de la methode get().
 */
public interface Reader<E> {

	/**
	 * 
	 * @author CHU Jonathan
	 * Etat du processus de lecture d'un ByteBuffer.
	 */
	public static enum ProcessStatus {
		DONE, REFILL, ERROR
	};

	/**
	 * Lis un ByteBuffer et stocke des informations selon le type reel du Reader.
	 * @return
	 */
	ProcessStatus process();

	/**
	 * Renvoie la donnee stockee par le Reader.
	 * @return la donnee stockee par le Reader.
	 */
	E get();

	/**
	 * Reinitialise le Reader.
	 */
	void reset();
}
