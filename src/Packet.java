import java.nio.ByteBuffer;

public class Packet {
	public static final int PACKET_SIZE = 2048;
	
	public static final int TYPE_PASSWORD_REQUEST = 1;
	public static final int TYPE_USNAME_REQUEST = 2;
	public static final int TYPE_DATA_TRANSFER = 3;
	public static final int TYPE_DATA_ACKNOWLEDGE =4;
	public static final int TYPE_FILE_INFO = 5;
	public static final int TYPE_ERROR = 6;
	
	public static final String ERROR_FILE_NOT_FOUND = "FILE_NOT_FOUND";
	public static final String ERROR_REACHED_MAX_RETRY = "REACHED_MAX_RETRY";
	
	private static final int TYPE_START = 0;
	private static final int TYPE_END = 4;
	
	private static final int HASH_START = TYPE_END;
	private static final int HASH_END = 132;
	
	private static final int DATA_START = HASH_END;
	private static final int DATA_END = PACKET_SIZE;
	
	private byte[] data = new byte[PACKET_SIZE];
	private ByteBuffer bb = ByteBuffer.wrap(data);
	
	public Packet(int type){
		bb.asIntBuffer().put(0, type);
	}
	
	public Packet(byte[] b){
		System.arraycopy(b, 0,data, 0, data.length);
	}
	
	public void putDataSection(byte[] b){
		if(b.length > DATA_END - DATA_START){
			System.out.println("ERROR ATTEMTPING TO PUT MORE DATA THAN SIZE ALLOWS");
		}
		System.arraycopy(b, 0, data, DATA_START, b.length);
	}
	
	public byte[] getDataSection(){
		byte[] b = new byte[DATA_END - DATA_START];
		System.arraycopy(data, DATA_START, b, 0, b.length); //DO BASE 64 ENCODING HERE
		return b;
	}
	
	public String getDataSectionAsString(){
		StringBuilder sb = new StringBuilder();
		for(int i = DATA_START; i < DATA_END; i++){
			if(data[i] != 0){
				sb.append((char)(data[i]));
			}
		}
		return sb.toString();
	}
	
	public byte[] getRawData(){ //get raw bytes
		return data;
	}
	
	public int getType(){
		return bb.asIntBuffer().get(0);
	}
	
	public String toString(){
		String retS = "TYPE: " + bb.asIntBuffer().get(0);
		retS+="\n HASH: ";
		for(int i = HASH_START; i < HASH_END; i++){
			retS+=data[i];
		}
		retS+="\n DATA: ";
		for(int i = DATA_START; i < DATA_END; i++){
			retS+=data[i]+"|";
		}
		return retS;
	}
}
