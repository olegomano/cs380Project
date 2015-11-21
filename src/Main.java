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
		byte[] tstkey = {1,2,3};
		byte[] data = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
		
		String original = "|";
		for(int i = 0; i < data.length; i++){
			original+= data[i] + "|";
		}
		System.out.println("Origin\n" + original);
		
		
		byte[] encrypted = Utils.encrypt(data, tstkey);
		String enc = "|";
		for(int i = 0; i < encrypted.length; i++){
			enc+= encrypted[i] + "|";
		}
		System.out.println("ENCRYPTED\n" + enc);
		
		byte[] decrypted = Utils.encrypt(encrypted, tstkey);
		String dec = "|";
		for(int i = 0; i < decrypted.length; i++){
			dec+= decrypted[i] + "|";
		}
		System.out.println("DECRYPTED\n" + dec);
		
		
		
		if(args[0].compareTo(SERVER_MODE) == 0){
			
			Server server = new Server();
			server.start();
			FILE_PATH = args[1];
		//	KEY_PATH = args[2];
			
		}else if(args[0].compareTo(CLIENT_MODE) ==0 ){
			Client client = new Client();
			client.connect("localhost");	
	//		KEY_PATH = args[1];
		}else{
			System.out.println("Invalid parameters");
		}
		
	}

}
