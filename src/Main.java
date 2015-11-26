import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
	/**
	 * @param args
	 * @throws IOException 
	 */
	
	private static String F_PATH = "-f";
	private static String KEY_PATH = "-k";
	private static String SERVER_MODE = "-s";
	private static String CLIENT_MODE = "-c";
	private static String ASCII_ARMOR = "-a";
	private static String HASH_FAIL = "-h";
	private static String COMMAND_LIST = "-help";
	private static String PORT_NUMBER = "-p";
	private static String SERVER_IP = "-i";
	
	public static boolean FAIL_HASH = false;
	public static String FILE_PATH = "";	
	public static byte[] KEY;
	public static boolean ASCII_ARMORED = false;
	
	
	
	public static void main(String[] args) throws IOException {
		Server.SERVER_PORT= getPortNumber(args);
		loadKey(getKeyPath(args));
		ASCII_ARMORED = asciiArmored(args);
		FILE_PATH = getFilePath(args);
		FAIL_HASH = isHashFailed(args);
		
		if(FILE_PATH == null){
			System.out.println("ERROR no file specified");
			return;
		}
		if(KEY == null){
			System.out.println("ERROR no key specified");
		}
		
		if(isClient(args)){
			Client client = new Client();
			client.connect(getServerIp(args));
		}else{
			Server server = new Server();
			server.start();
		}
		
	}
	
	public static String getServerIp(String[] arr){
		for(int i = 0; i < arr.length; i++){
			if(arr[i].compareTo(SERVER_IP) == 0){
				return arr[i+1]; 
			}
		}
		return "localhost";
	}
	
	private static int getPortNumber(String[] arr){
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(PORT_NUMBER) == 0){
				return Integer.parseInt(arr[i + 1]);
			}
		}
		return -1;
		
		
	}
	
	private static boolean isClient(String[] arr){
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(SERVER_MODE) == 0){
				return false;
			}
		}
		return true;
	}
	
	private static boolean asciiArmored(String[] arr){
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(ASCII_ARMOR) == 0){
				return true;
			}
		}
		return false;
	}
	
	private static boolean isHashFailed(String[] arr){
		int mode = -1;
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(HASH_FAIL) == 0){
				mode = i;
			}
		}
		return !(mode == -1);
	}
	
	private static String getKeyPath(String[] arr){
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(KEY_PATH) == 0){
				return arr[i+1];
			}
		}
		return null;
	}
	
	private static String getFilePath(String[] arr){
		for(int i =0; i < arr.length; i++){
			if(arr[i].compareTo(F_PATH) == 0){
				return arr[i+1];
			}
		}
		return null;
	}
	
	
	
	private static void loadKey(String path) throws IOException{
		File f = new File(path);
		FileInputStream in = new FileInputStream(f);
		KEY = new byte[in.available()];
		System.out.println("Read key length: " + KEY.length);
		in.read(KEY);
	}

}
