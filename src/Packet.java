import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
	public static final int PACKET_SIZE = 2052;

	public static final int TYPE_PASSWORD_REQUEST = 1;
	public static final int TYPE_USNAME_REQUEST = 2;
	public static final int TYPE_DATA_TRANSFER = 3;
	public static final int TYPE_DATA_ACKNOWLEDGE =4;
	public static final int TYPE_FILE_INFO = 5;
	public static final int TYPE_ERROR = 6;
	public static final int TYPE_TRANSFER_COMPLETE = 7;
	public static final int TYPE_CONNECTION_CLOSED = 8;

	public static final String ERROR_FILE_NOT_FOUND = "FILE_NOT_FOUND";
	public static final String ERROR_REACHED_MAX_RETRY = "REACHED_MAX_RETRY";

	private static final int TYPE_START = 0;
	private static final int TYPE_END = 4;

	private static final int SIZE_START = TYPE_END;
	private static final int SIZE_END = SIZE_START + 4;

	private static final int HASH_START = SIZE_END;
	private static final int HASH_END = HASH_START + 8;

	private static final int DATA_START = HASH_END;
	private static final int DATA_END = PACKET_SIZE;

	public static int DATA_SECTION_MAX = (DATA_END - DATA_START)*3/4;
	
	
	
	private byte[] data = new byte[PACKET_SIZE];
	private ByteBuffer bb = ByteBuffer.wrap(data);

	public Packet(int type){
		bb.asIntBuffer().put(0, type);
	}

	public Packet(byte[] b){
		System.arraycopy(b, 0,data, 0, b.length);
	}
	public double getHash(byte[] b)
	{
		int hashBrown = 2;
		for(int a = 0; a < b.length; a++)
		{
			hashBrown += (int) Math.pow(2,a);
			hashBrown = ((hashBrown >> 5) + hashBrown);
		}
		return hashBrown;
	}
	public void putDataSection(byte[] b){
		if(b.length > DATA_END - DATA_START){
			System.out.println("ERROR ATTEMTPING TO PUT MORE DATA THAN SIZE ALLOWS");
		}
		else{
			Utils encoder = new Utils();
			byte[] padded = new byte[DATA_SECTION_MAX];
			System.arraycopy(b,0,padded,0,b.length);
			double hash = getHash(padded);
			byte[] asciiArmored = new byte[DATA_SECTION_MAX];
			if(Main.ASCII_ARMORED){
				asciiArmored = encoder.encodeBase64(padded);
			}else{
				System.arraycopy(padded,0,asciiArmored,0,DATA_SECTION_MAX);
			}
			byte[] encrypted = encoder.encrypt(asciiArmored,Main.KEY);
			byte[] hashB = new byte[8];
			ByteBuffer.wrap(hashB).putDouble(hash);
			System.arraycopy(hashB, 0, data, HASH_START, hashB.length);
			System.arraycopy(encrypted, 0, data, DATA_START, encrypted.length);
		}
	}

	public void putDataSection(byte[] b, int size){
		byte size0 = (byte) ( (size & 0xFF000000) >> 24 );
		byte size1 = (byte) ( (size & 0xFF0000) >> 16 );
		byte size2 = (byte) ( (size & 0xFF00) >> 8 );
		byte size3 = (byte) ( (size & 0xFF) >> 0 );
		data[SIZE_START]     = size0;
		data[SIZE_START + 1] = size1;
		data[SIZE_START + 2] = size2;
		data[SIZE_START + 3] = size3;
		putDataSection(b);
	}

	public boolean validateHash(){
		byte[] data = getDataSection();
		byte[] savedHash = new byte[HASH_END - HASH_START];
		System.arraycopy(data,HASH_START,savedHash,0,savedHash.length);
		double dataHash = getHash(data);
		
		System.out.println("Hash From data: " + dataHash + "Hash from Packet " + ByteBuffer.wrap(savedHash).getDouble());
		if(dataHash == ByteBuffer.wrap(savedHash).getDouble())
			return true;
		return true;
	}

	public int getSize(){
		int size = (byteToInt(data[SIZE_START]) << 24 ) | (byteToInt(data[SIZE_START + 1]) << 16 ) |(byteToInt(data[SIZE_START + 2]) << 8 )|(byteToInt(data[SIZE_START + 3]) << 0 );
		return size;
	}

	private int byteToInt(byte b){
		return (int) b & 0xFF;
	}

	public byte[] getDataSection(){
		byte[] b = new byte[DATA_END - DATA_START];
		byte[] hash = new byte[HASH_END-HASH_START];
		System.arraycopy(data, HASH_START, hash, 0, hash.length);
		System.arraycopy(data, DATA_START, b, 0, b.length);
		byte[] unencrypted = Utils.decrypt(b,Main.KEY);
		if(Main.ASCII_ARMORED){
			byte[] decoded = Utils.decodeBase64(unencrypted);
			return decoded;
		}
		return unencrypted;
		
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
		retS+="\n DATA size: " + getSize();
		return retS;
	}
}
