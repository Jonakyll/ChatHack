package chatHack.client;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedPrivateClients {
	
	private final Map<String, Long> clients = new HashMap<>();
	private final Object monitor = new Object();
	
	public boolean containsKey(String dst) {
		synchronized (monitor) {
			return clients.containsKey(dst);
		}
	}

}
