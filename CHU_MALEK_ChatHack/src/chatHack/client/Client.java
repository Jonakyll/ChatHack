package chatHack.client;

public class Client {
	
	private final String ip;
	private final int port;
	private final long token;
	
	public Client(String ip, int port, long token) {
		this.ip = ip;
		this.port = port;
		this.token = token;
	}

}
