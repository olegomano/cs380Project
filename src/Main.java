import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
	/**
	 * @param args
	 * @throws IOException 
	 */
	private static String SERVER_MODE = "-s";
	private static String CLIENT_MODE = "-c";
	
	public static String FILE_PATH = "";
	public static String KEY_PATH = "";
	public static void main(String[] args) throws IOException {
		if(args[0].compareTo(SERVER_MODE) == 0){
			
			Server server = new Server();
			server.start();
			FILE_PATH = args[1];
			KEY_PATH = args[2];
			
		}else if(args[0].compareTo(CLIENT_MODE) ==0 ){
			Client client = new Client();
			client.connect("localhost");	
			KEY_PATH = args[1];
		}else{
			System.out.println("Invalid parameters");
		}
		
	}

}
