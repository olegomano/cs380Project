


//import IllegalArgumentException;
//import String;

 
public class Utils {
	/*
	public static void main(String[] args){
		byte[] tempArray = {'A', '2', '3', 'B', '2', '2', '1'};
		byte[] tempKey = {28, 24, 21, 22, 64};
		System.out.println(new String(decrypt(encrypt(encodeBase64(tempArray), tempKey), tempKey)));
		System.out.println(new String(encodeBase64(tempArray)));
	}
	*/
	public static byte[] encodeBase64(byte[] in){
		/*
		 * encodes the given byte array into an acceptable 
		 * format for transmission.
		 * create array that is a multiple of 3 bytes
		 * separate bytes into 6 bit segments then translate to bytes again
		 * return the new byte array created by this process
		 */
		byte[] padded = new byte[in.length + (3-in.length%3)];
		System.arraycopy(in, 0, padded, 0, in.length);

		byte[] out = new byte[(padded.length*4)/3];
		int outCounter = 0;
		int n;
		//System.out.println("out array length:"+out.length);
		for(int i = 0;i < in.length;i += 3){
			n = (padded[i] & 0xFC) >> 2;
			System.out.println(n);
			out[outCounter] = (byte) (n + 65);
			n = (padded[i] & 0x03) << 4;
			if(i+1<in.length){
				n |= (padded[i+1] & 0xF0) >> 4;
				//create second "byte"/chunk
				out[outCounter+1] = (byte)(n+65);
				n = (padded[i+1] & 0x0F) << 2;
				if(i+2<in.length){
					n |= (padded[i+2] & 0xC0) >> 6;
					out[outCounter+2] = (byte)(n + 65);
					n = (padded[i+2] & 0x3F);
					out[outCounter+3] = (byte)(n + 65);
					outCounter+=4;
				}else{
					out[outCounter+2] = (byte)(n + 65);
					out[outCounter+3] = '=';
				}
			}else{
				out[outCounter+1] = (byte)(n + 65);
				out[outCounter+2] = '=';
				out[outCounter+3] = '=';
			}
		}

		return out;
	}

	public static byte[] decodeBase64(byte[] in){
		if(in.length % 4 != 0){
			throw new IllegalArgumentException("invalid input");
			//what else should this do for communication?
		}
		/*
		 *  (input.indexOf('=') > 0 ? (input.length() - input.indexOf('=')) : 0)];
		 *  find out how to implement this
		 */
		int fillerLength = (new String(in).indexOf('='));
		if(fillerLength > 0){
			fillerLength = in.length - fillerLength;
		}else{
			//redundant possibly
			fillerLength = 0;
		}
		System.out.println("filler length"+fillerLength);
		int outCounter = 0;
		byte[] out = new byte[(in.length*3)/4 - fillerLength];
		System.out.println("length of output array:"+out.length);
		for(int i = 0; i<in.length; i+=4){
			out[outCounter] = (byte)(((in[i]-65) << 2) | ((in[i+1]-65) >> 4));
			if(outCounter+1 < out.length){
				out[outCounter+1] = (byte)(((in[i+1]-65) << 4) | ((in[i+2]-65) >> 2));
				if(outCounter+2 < out.length){
					out[outCounter+2] = (byte)(((in[i+2]-65) << 6) | (in[i+3]-65));
				}

			}
			outCounter+=3;
		}
		return out;
	}

	public static byte[] encrypt(byte[] in, byte[] key){
		byte[] out = new byte[in.length];
		for(int i = 0; i<in.length; i+= key.length){
			out[i] = (byte)(toInt(in[i])^toInt(key[i%key.length]));
		}
		return out;
	}

	public static byte[] decrypt(byte[] in, byte[] key){
		return encrypt(in, key);
	}

	public static int toInt(byte b){
        return ((int) b) & 0xff;
    }

}
