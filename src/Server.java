import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.Charset;


public class Server {
	public static final int SERVER_PORT = 4445;
	private ServerThread server;
	
	private final static String username = "oleg";  //TODO: read this dynamicall from file
	private final static String password = "tolstov";
	
	public void start() throws IOException{
		server = new ServerThread();
		server.start();
	}
	
	public void sendPacket(){
		
	}
	
	private class ServerThread extends Thread{
		private ServerSocket mSocket;
		private Socket clientSocket;
		private OutputStream toClient;
		private InputStream  fromClient;
		public void run(){
			try {
				mSocket = new ServerSocket(SERVER_PORT);
				System.out.println("Listening on port: " + SERVER_PORT +" ");
				clientSocket = mSocket.accept();
				System.out.println("Got connection from: " + clientSocket.getInetAddress());
				
				toClient = clientSocket.getOutputStream();
				fromClient = clientSocket.getInputStream();
				while(!requestUsername()){}
				while(!requestPassword()){};
				sendFile("");
				
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		private boolean requestUsername() throws IOException, InterruptedException{
			Packet p = new Packet(Packet.TYPE_USNAME_REQUEST);
			toClient.write(p.getRawData());		
			byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(recievedPacket)==-1){};//wait for responce
			Packet fClient = new Packet(recievedPacket);
			System.out.println("recieved packet from client " + fClient.toString());
			String usernameString = fClient.getDataSectionAsString();
			int comparison = usernameString.compareToIgnoreCase(Server.username); 
			if( comparison == 0){
				return true;
			}
			return false;
			
		}
		
		private boolean requestPassword() throws IOException{
			Packet p = new Packet(Packet.TYPE_PASSWORD_REQUEST);
			toClient.write(p.getRawData());		
			byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(recievedPacket)==-1){};//wait for responce
			Packet fClient = new Packet(recievedPacket);
			System.out.println("recieved packet from client " + fClient.toString());
			String usernameString = fClient.getDataSectionAsString();
			int comparison = usernameString.compareToIgnoreCase(Server.password); 
			if( comparison == 0){
				return true;
			}
			return false;
		}
		
		private void sendFile(String path) throws IOException{
			File toSend = new File(path);
			if(!toSend.canRead() || !toSend.exists()){
				System.out.println("ERROR: file to send does not exist, or do not have read privelages");
				sendErrorMsg();
				return;
			}
		}
		
		private void sendErrorMsg() throws IOException{
			Packet p = new Packet(Packet.TYPE_ERROR);
			toClient.write(p.getRawData());
		}
		
	}
}
