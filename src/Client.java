import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Scanner;


public class Client {
	private Socket server;
	private OutputStream toServer;
	private InputStream fromServer;
	private OutputStream file;
	private Scanner keyboard = new Scanner(System.in);
	
	private boolean fileCreated = false;
	private File downloadFile;
	private String workingDir = System.getProperty("user.dir");
	
	private boolean transfering = true;
	public void connect(String hostname) throws UnknownHostException, IOException{
		server = new Socket(hostname,Server.SERVER_PORT);
		toServer = server.getOutputStream();
		fromServer = server.getInputStream();
		byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
		while(transfering){
			int read = fromServer.read(recievedPacket);
			if(read != -1){
				System.out.println("Read data size: " + read + " expected size " + Packet.PACKET_SIZE);
				onPacketRecieved(new Packet(recievedPacket));
			}
		}
		server.close();
		file.close();
	}
	
	private void onPacketRecieved(Packet p) throws IOException{
		System.out.println("Client recieved packet: " + p.toString());	
		switch(p.getType()){
		case Packet.TYPE_USNAME_REQUEST:
			System.out.println("Enter username: ");
			String usname = keyboard.nextLine();
			Packet usnamePacket = new Packet(Packet.TYPE_DATA_TRANSFER);
			usnamePacket.putDataSection(usname.getBytes(),usname.getBytes().length);
			toServer.write(usnamePacket.getRawData());
			break;
		case Packet.TYPE_PASSWORD_REQUEST:
			System.out.println("Enter password: ");
			String password = keyboard.nextLine();
			Packet passPacket = new Packet(Packet.TYPE_DATA_TRANSFER);
			passPacket.putDataSection(password.getBytes(),password.getBytes().length);
			toServer.write(passPacket.getRawData());
			break;
		case Packet.TYPE_DATA_TRANSFER:
			if(p.validateHash()){
				file.write(p.getDataSection(),0,p.getSize());
				file.flush();
				Packet responce = new Packet(Packet.TYPE_DATA_ACKNOWLEDGE);
				toServer.write(responce.getRawData());
			}else{
				Packet responce = new Packet(Packet.TYPE_ERROR);
				toServer.write(responce.getRawData());	
			}
			break;
		case Packet.TYPE_FILE_INFO:
			if(!fileCreated){
				System.out.println("Recieved file info from server");
				byte[] fName = new byte[p.getSize()];
				System.arraycopy(p.getDataSection(),0,fName,0,fName.length);
				System.out.println("Creating new File " + Main.FILE_PATH + "/"+new String(fName));
				downloadFile = new File(Main.FILE_PATH+"/recieved"+new String(fName));
				if(downloadFile.exists()){
					downloadFile.delete();
				}
				if(!downloadFile.createNewFile() ){
					System.out.println("Failed creating download file");
				}
				file = new FileOutputStream(downloadFile);
				fileCreated = true;
			}
			Packet res = new Packet(Packet.TYPE_DATA_ACKNOWLEDGE);
			toServer.write(res.getRawData());
			break;
		case Packet.TYPE_ERROR:	
			System.out.println("There was an error");
		case Packet.TYPE_CONNECTION_CLOSED:
			System.out.println("Server closed connection!");
			transfering = false;
			break;
		case Packet.TYPE_TRANSFER_COMPLETE:
			System.out.println("Transfer succesfull!");
			transfering = false;
			break;
			
		}
	}

}
