package chatHack.server;

import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SynchronizedClients<E> {
	
	private final Map<E, SelectionKey> clients = new HashMap<>();
	private final Object monitor = new Object();
	
	public Collection<SelectionKey> values() {
		synchronized (monitor) {
			return clients.values();
		}
	}
	
	public SelectionKey put(E e, SelectionKey key) {
		synchronized (monitor) {
			return clients.put(e, key);
		}
	}
	
	public SelectionKey get(E e) {
		synchronized (monitor) {
			return clients.get(e);
		}
	}
	
	public Set<E> keySet() {
		synchronized (monitor) {
			return clients.keySet();
		}
	}
	
	public SelectionKey remove(E e) {
		synchronized (monitor) {
			return clients.remove(e);
		}
	}
	
}
