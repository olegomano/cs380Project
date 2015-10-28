import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	public static final int SERVER_PORT = 4444;
	
	private ServerThread server;
	public void start() throws IOException{
		server = new ServerThread();
		server.start();
	}
	
	public void sendPacket(){
		
	}
	
	private class ServerThread extends Thread{
		private ServerSocket mSocket;
		private Socket clientSocket;
		private OutputStream socketStream;
		
		public void run(){
			try {
				mSocket = new ServerSocket(SERVER_PORT);
				System.out.println("Listening on port: " + SERVER_PORT +" ");
				clientSocket = mSocket.accept();
				System.out.println("Got connection from: " + clientSocket.getInetAddress());
				socketStream = clientSocket.getOutputStream();
				InputStream fromClient = clientSocket.getInputStream();
				int read = 0;
				byte[] data = new byte[1024];
				while(true){
					fromClient.read(data);
					System.out.println("READ DATA: " + new String(data));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		
	}
}
