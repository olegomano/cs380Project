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
	private Scanner keyboard = new Scanner(System.in);
	
	
	public void connect(String hostname) throws UnknownHostException, IOException{
		server = new Socket(hostname,Server.SERVER_PORT);
		
		toServer = server.getOutputStream();
		fromServer = server.getInputStream();
		
		byte[] recievedPacket = new byte[Packet.PACKET_SIZE];
		while(true){
			int read = fromServer.read(recievedPacket);
			if(read != -1){
				System.out.println("Read data size: " + read + " expected size " + Packet.PACKET_SIZE);
				onPacketRecieved(new Packet(recievedPacket));
			}
		}
		
	}
	
	private void onPacketRecieved(Packet p) throws IOException{
		System.out.println("Client recieved packet: " + p.toString());
		
		switch(p.getType()){
		case Packet.TYPE_USNAME_REQUEST:
			System.out.println("Enter username: ");
			String usname = keyboard.nextLine();
			Packet usnamePacket = new Packet(Packet.TYPE_DATA_TRANSFER);
			usnamePacket.putDataSection(usname.getBytes());
			toServer.write(usnamePacket.getRawData());
			break;
		case Packet.TYPE_PASSWORD_REQUEST:
			System.out.println("Enter password: ");
			String password = keyboard.nextLine();
			Packet passPacket = new Packet(Packet.TYPE_DATA_TRANSFER);
			passPacket.putDataSection(password.getBytes());
			toServer.write(passPacket.getRawData());
			break;
		case Packet.TYPE_DATA_TRANSFER:
			
			break;
		}
	}

}
