/**
 * @Project: VSP_4
 * @File:    Message.java
 * @Package: station
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    20.12.2016
 */
package Station;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Message {
	public static final int MESSAGELENGTH = 34;
	public static final int DATALENGTH = 24;
	
	private byte[] rawMsg;
	private InetAddress from;
	private boolean msgValid = false;
	
	public static ByteBuffer bybu = ByteBuffer.allocate(Long.BYTES);
	
	public Message(byte[] msg, InetAddress addr) {
		if(msg.length == MESSAGELENGTH) {
			rawMsg = new byte[MESSAGELENGTH];
			System.arraycopy(msg, 0, rawMsg, 0, MESSAGELENGTH);
			from = addr;
			msgValid = true;
		} else {
			System.out.println("ERROR: Messagesize is not valid. ("
							   + msg.length + " != " + MESSAGELENGTH + ")");
		}
	}
	
	public Message(char type, String msg, int slotID, InetAddress addr) {
		if(msg.length() == DATALENGTH) {
			rawMsg = new byte[MESSAGELENGTH];
			from = addr;
			msgValid = true;
			
			byte[] msgb = msg.getBytes(Charset.forName("UTF-8"));
			byte[] timeb = longToByteArray(System.currentTimeMillis());
			
			rawMsg[0] = (new String(""+type).getBytes(Charset.forName("UTF-8")))[0];
			System.arraycopy(msgb, 0, rawMsg, 1, DATALENGTH);
			rawMsg[25] = (byte)slotID;
			System.arraycopy(timeb, 0, rawMsg, 26, 8);
		} else {
			System.out.println("ERROR: Datasize is not valid. ("
					   		   + msg.length() + " != " + DATALENGTH + ")");
		}
	}
	
	public Message(char type, byte[] msg, int slotID, InetAddress addr) {
		if(msg.length == DATALENGTH) {
			rawMsg = new byte[MESSAGELENGTH];
			from = addr;
			msgValid = true;
			
			byte[] timeb = longToByteArray(System.currentTimeMillis());
			
			rawMsg[0] = (new String(""+type).getBytes(Charset.forName("UTF-8")))[0];
			System.arraycopy(msg, 0, rawMsg, 1, DATALENGTH);
			rawMsg[25] = (byte)slotID;
			System.arraycopy(timeb, 0, rawMsg, 26, 8);
		} else {
			System.out.println("ERROR: Datasize is not valid. ("
					   		   + msg.length + " != " + DATALENGTH + ")");
		}
	}
	
	public byte[] getMessage() {
		if(msgValid) {
			//System.out.println( new String(rawMsg, Charset.forName("UTF-8")) );
			return rawMsg;
		} else {
			return null;
		}
	}
	
	public byte[] getMessageNow(int slot, long time) {
		if(msgValid) {
			byte[] timeb = longToByteArray(time);
			System.arraycopy(timeb, 0, rawMsg, 26, 8);
			rawMsg[25] = (byte)slot;
			
			return rawMsg;
		} else {
			return null;
		}
	}
	
	public InetAddress getInetAddress() {
		return from;
	}
	
	@Override
	public String toString() {
		return String.valueOf(rawMsg);
	}
	
	private byte[] longToByteArray(long value) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(value);
	    return buffer.array();
	}
}
