package chatHack.client;

import java.nio.channels.SelectionKey;

public class Client {
	
	private final long token;
	private final SelectionKey key;
	
	public Client(long token, SelectionKey key) {
		this.token = token;
		this.key = key;
	}

	public long getToken() {
		return token;
	}
	
	public SelectionKey getKey() {
		return key;
	}
}
