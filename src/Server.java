import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;


public class Server {
	public static int SERVER_PORT = 10114;
	private ServerThread server;
	
	private volatile HashMap<String,String> useNameList = new HashMap();
	
	private final static int SUCCESS =  0;
	private final static int FAILURE = -1;
	private final static int COMPLETE = 1;
	
	
	
	public void start() throws IOException{
		loadPasswordList();
		server = new ServerThread();
		server.start();
	}
	
	private void loadPasswordList() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader((new File("PasswordList.txt"))));
		String line = in.readLine();
		while(line!=null){
			String[] split = line.split(",");
			useNameList.put(split[0], split[1]);
			line = in.readLine();
		}	
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
				clientSocket.setSoTimeout(50000);
				toClient = clientSocket.getOutputStream();
				fromClient = clientSocket.getInputStream();
				while(!requestUsername()){}
				while(!requestPassword()){};
				sendFile(Main.FILE_PATH);
				Thread.sleep(1000);
				toClient.close();
				fromClient.close();
				clientSocket.close();
				mSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		private String useName = "";
		private boolean requestUsername() throws IOException, InterruptedException{
			Packet p = new Packet(Packet.TYPE_USNAME_REQUEST);
			toClient.write(p.getRawData());		
			byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(recievedPacket)==-1){};//wait for responce
			Packet fClient = new Packet(recievedPacket);
			//System.out.println("recieved packet from client " + fClient.toString());
			byte[] usefullBits = new byte[fClient.getSize()];
			System.out.println("getDataSection called from Server");
			System.arraycopy(fClient.getDataSection(), 0, usefullBits, 0,usefullBits.length);
			String usernameString = new String(usefullBits);
			System.out.println("Recived Username From client: " + usernameString);
			useName = usernameString;
			if(useNameList.get(usernameString) ==null){
				return false;
			}
			return true;
			
		}
		
		private boolean requestPassword() throws IOException{
			Packet p = new Packet(Packet.TYPE_PASSWORD_REQUEST);
			toClient.write(p.getRawData());		
			byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(recievedPacket)==-1){};//wait for responce
			Packet fClient = new Packet(recievedPacket);
			byte[] usefullBits = new byte[fClient.getSize()];
			System.out.println("getDataSection called from Server");
			System.arraycopy(fClient.getDataSection(), 0, usefullBits, 0,usefullBits.length);
			String passwordString = new String(usefullBits);
			System.out.println("Recived Password From client: " + passwordString);
			if(useNameList.get(useName).compareTo(passwordString) == 0){
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
			int frameNumber = 0;
			int result = sendFileFrame(file);
			while(result == SUCCESS){
				System.out.println("Sending frame " + frameNumber++);
				result = sendFileFrame(file);
			}
			if(result == FAILURE){
				Packet fail = new Packet(Packet.TYPE_CONNECTION_CLOSED);
				toClient.write(fail.getRawData());
				System.out.println("ERROR TRANSFERRING FILE");
			}else{
				System.out.println("SUCCESFULLY TRASNFERED FILE");
				Packet success = new Packet(Packet.TYPE_TRANSFER_COMPLETE);
				toClient.write(success.getRawData());
			}
			
		}
		
		private boolean sendFileInfo(File f) throws IOException{
			Packet fileInfo = new Packet(Packet.TYPE_FILE_INFO);
			fileInfo.putDataSection(f.getName().getBytes(),f.getName().getBytes().length);
			toClient.write(fileInfo.getRawData());
			
			byte[] responce = new byte[Packet.PACKET_SIZE];
			while(fromClient.read(responce)==-1){};
			
			Packet rPacket = new Packet(responce);
			if(rPacket.getType() == Packet.TYPE_DATA_ACKNOWLEDGE){
				return true;
			}
			return false;
		}
		
		private int frameNumber = 0;
		private int sendFileFrame(InputStream f) throws IOException{
			byte[] toSend = new byte[Packet.DATA_SECTION_MAX];
			int read = f.read(toSend);
			//System.out.println("Server Read: " + read + " bytes");
			if(read == -1){
				return COMPLETE;
			}
			
			Packet fileFrame = new Packet(Packet.TYPE_DATA_TRANSFER);
			fileFrame.putDataSection(toSend,read);
			System.out.println(fileFrame.toString());
			toClient.write(fileFrame.getRawData());
			
			int retryCount = 0;
			byte[] responce = new byte[Packet.PACKET_SIZE];
			while(retryCount < 3){
				while(fromClient.read(responce) == -1){};
				Packet p = new Packet(responce);
				if(p.getType() == Packet.TYPE_DATA_ACKNOWLEDGE){
					return SUCCESS;
				}
				retryCount++;
				System.out.println("RESENDING PACKET, ATTEMPT " + retryCount);
				toClient.write(fileFrame.getRawData());
			}
			return FAILURE;
		}
		
		private void sendErrorMsg() throws IOException{
			Packet p = new Packet(Packet.TYPE_ERROR);
			toClient.write(p.getRawData());
		}
		
	}
}
