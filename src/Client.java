import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
	private Socket server;
	public void connect(String hostname) throws UnknownHostException, IOException{
		server = new Socket(hostname,Server.SERVER_PORT);
		System.out.println("Enter username: ");
		Scanner keyboard = new Scanner(System.in);
		String usName = keyboard.nextLine();
		String pass = keyboard.nextLine();
		
		OutputStream toServer = server.getOutputStream();
		toServer.write(usName.getBytes());
		toServer.write(pass.getBytes());
		
		
	}
	
	private void onMessageRecieved(String msg){
		
	}

}
