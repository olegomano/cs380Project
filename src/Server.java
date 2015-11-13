import java.io.File;
import java.io.FileInputStream;
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
	
	private final static int SUCCESS =  0;
	private final static int FAILURE = -1;
	private final static int COMPLETE = 1;
	
	
	
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
				sendFile("circuitwall.jpg");
				
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
			
			InputStream file = new FileInputStream(toSend);
			int infoRetryCount = 0;
			while(!sendFileInfo(toSend) && infoRetryCount < 3){
				infoRetryCount++;
				System.out.println("ERROR: failed sending file info, Retrying");
			}
			if(infoRetryCount==3){
				System.out.println("Failed sending file info, aborting connection");
				clientSocket.close();
				mSocket.close();
			}
			int result = sendFileFrame(file);
			while(result == SUCCESS){
				result = sendFileFrame(file);
			}
			if(result == FAILURE){
				System.out.println("ERROR TRANSFERRING FILE");
			}else{
				System.out.println("SUCCESFULLY TRASNFERED FILE");
			}
			
		}
		
		private boolean sendFileInfo(File f) throws IOException{
			Packet fileInfo = new Packet(Packet.TYPE_FILE_INFO);
			fileInfo.putDataSection(f.getName().getBytes());
			toClient.write(fileInfo.getRawData());
			
			byte[] responce = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(responce)==-1){};
			
			Packet rPacket = new Packet(responce);
			if(rPacket.getType() == Packet.TYPE_DATA_ACKNOWLEDGE){
				return true;
			}
			return false;
		}
		
		private int sendFileFrame(InputStream f) throws IOException{
			byte[] toSend = new byte[Packet.DATA_SECTION_MAX];
			int read = f.read(toSend);
			System.out.println("Server Read: " + read + " bytes");
			if(read == -1){
				return COMPLETE;
			}
			
			Packet packet = new Packet(Packet.TYPE_DATA_TRANSFER);
			packet.putDataSection(toSend,read);
			toClient.write(packet.getRawData());
			
			int retryCount = 0;
			boolean success = false;
			byte[] responce = new byte[Packet.PACKET_SIZE];
			while(retryCount < 3 && !success){
				while(fromClient.read(responce) == -1){};
				Packet p = new Packet(responce);
				if(p.getType() == Packet.TYPE_DATA_ACKNOWLEDGE){
					success = true;
				}
				retryCount++;
			}
			if(!success){return FAILURE;}
			
			return SUCCESS;
		}
		
		private void sendErrorMsg() throws IOException{
			Packet p = new Packet(Packet.TYPE_ERROR);
			toClient.write(p.getRawData());
		}
		
	}
}