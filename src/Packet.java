import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	private static final int SIZE_END = SIZE_START + 8;

	private static final int HASH_START = SIZE_END;
	private static final int HASH_END = HASH_START + 8;

	private static final int DATA_START = HASH_END;
	private static final int DATA_END = PACKET_SIZE;

	public static int DATA_SECTION_MAX = (DATA_END - DATA_START)*3/4;
	
	
	
	private byte[] data = new byte[PACKET_SIZE];
	private ByteBuffer bb = ByteBuffer.wrap(data);

	public Packet(int type){
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.asIntBuffer().put(0, type);
	}

	public Packet(byte[] b){
		bb.order(ByteOrder.BIG_ENDIAN);
		System.arraycopy(b, 0,data, 0, b.length);
	}
	public static double getHash(byte[] b){
		System.out.println("hashing data length: " + b.length);
		int oneCount = 0, zeroCount = 0;
		double hashBrown = 0;
		for(int a = 0; a < b.length; a++){
			int ones = countOneInByte(b[a]);
			oneCount+=ones;
			zeroCount+=(8 - ones);
		}
		System.out.println(oneCount + "|" + zeroCount);
		for(int i = 0; i < b.length; i++)
		{
			if(i%2==0)
				hashBrown += b[i];
			else
				hashBrown -= b[i];
		}
		if(zeroCount > oneCount)
			hashBrown += zeroCount/(oneCount + 1);
		else
			hashBrown += oneCount/(zeroCount + 1);
		return hashBrown;
	}
	
	public static int countOneInByte(byte b){
		int oCount = 0;
		for(int i = 0; i < 8; i++){
			oCount+=b&(0x1);
			b = (byte) ( (b >> 1)&0xFF);
		}
		return oCount;
	}
	
	public static int getBit(byte[] data, int pos) {
		int posByte = pos/8;
		int posBit = pos%8;
		byte valByte = data[posByte];
		int valInt = valByte>>(8-(posBit+1)) & 0x0001;
		return valInt;
	}
	public void putDataSection(byte[] b){
		if(b.length > DATA_END - DATA_START){
			System.out.println("ERROR ATTEMTPING TO PUT MORE DATA THAN SIZE ALLOWS");
		}
		else{
			System.out.println("Input array size: " + b.length);
			byte[] padded = new byte[DATA_SECTION_MAX];
			System.out.println("Padded length: " + padded.length);
			System.arraycopy(b,0,padded,0,b.length);	
			double hash = getHash(padded);
			bb.putDouble(HASH_START, hash);
			byte[] asciiArmored;
			if(Main.ASCII_ARMORED){
				asciiArmored = Utils.encode(padded);
				System.out.println("Size after encoding: " + asciiArmored.length);
			}else{
				asciiArmored = new byte[DATA_END - DATA_START];
				System.arraycopy(padded,0,asciiArmored,0,padded.length);
			}
			byte[] encrypted = Utils.encrypt(asciiArmored,Main.KEY);
			System.out.println("Size after encrypting: " + encrypted.length);
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
		if(Main.FAIL_HASH){
			return false;
		}
		System.out.println("getData called from validateHash");
		byte[] data = getDataSection();
		double dataHash = getHash(data);
		double hashFromPacket = bb.getDouble(HASH_START);
		System.out.println("Hash from packet: " + hashFromPacket + "," + dataHash);
		if(dataHash == hashFromPacket)
			return true;
		return false;
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
		System.out.println("Size of data: " + b.length);
		System.arraycopy(data, DATA_START, b, 0, b.length);
		byte[] unencrypted = Utils.decrypt(b,Main.KEY);
		System.out.println("Size after unencryption " + unencrypted.length);
		if(Main.ASCII_ARMORED){
			byte[] decoded = Utils.decode(unencrypted);
			System.out.println("Size after decoding: " + decoded.length);
			System.out.println("GETDATA CALLLLLLLLLLLLLLLLLLLLLLLLLLLLLLED");
			for(int i=0; i<b.length;i++){
				System.out.println(b[i]);
			}
			return decoded;
		}
		byte[] paddedBack = new byte[DATA_SECTION_MAX];
		System.arraycopy(unencrypted,0,paddedBack,0,paddedBack.length);
		return paddedBack;
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
		retS+="\n Size: " + getSize();
		retS+="\n DATA: ";
		for(int i = DATA_START; i < DATA_END; i++){
			retS+=data[i]+"|";
		}
		retS+="\n DATA size: " + getSize();
		return retS;
	}
}
