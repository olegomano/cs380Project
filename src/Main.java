import java.io.IOException;
import java.net.UnknownHostException;


public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.start();
		Client client = new Client();
		client.connect("localhost");

	}

}
