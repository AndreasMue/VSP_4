/**
 * @Project: VSP_4
 * @File:    StationController.java
 * @Package: Station
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    17.12.2016
 */
package Station;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import Entrypoint.Entrypoint;


public class StationController extends Thread {
	public static final int FRAMELENGTH = 1000;
	public static final int SLOTCOUNT = 25;
	public static final int SLOTLENGTH = 40;
	
	public boolean running = true;
	
	/* Other relevant Components */
	private TransmitterClassA transmitter;
	private Receiver receiver;
	private SlotController slotCtrl;
	
	/* The last is also the current active one */
	private long lastFrameID = 0;
	private int lastSlotId = 0;
	
	/* Streams and bytearray to read data from datasource */
	private BufferedInputStream bis = new BufferedInputStream(System.in);
	private DataInputStream dis = new DataInputStream(bis);
	private byte[] data = new byte[Message.DATALENGTH];
	
	private int classAMsg;
	
	private long concOffset = 0;
	private long concOffsetClassB = 0;
	
	private long newFrameOffset = 0;
	private long frameOffset = 0;
	
	private long offset = 0;
	private long offsetClassB = 0;
	
	public StationController() {
		slotCtrl = new SlotController(25);
		
		receiver = new Receiver(this);
		transmitter = new TransmitterClassA(slotCtrl, this);
		
		transmitter.setPriority(Thread.MAX_PRIORITY);
		
		try {
			/* Test Message
			Message msg = new Message('A', "team 10-01 hgfdertfredre", 0, InetAddress.getByName(Entrypoint.baseAddress));
			transmitter.setMessage(msg);
			transmitter.gotData = true;
			*/
			
			lastFrameID = currentFrameId();
			
			transmitter.start();
			receiver.start();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			Message msg = null;
			try {
				msg = receiver.getMessage();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(msg != null) {
				
				/* Got new Message */
				handleMessage(msg);
			} 
			
			if(lastSlotId < (getCurrentTimeMillis() % 1000)/40 ) {
				
				lastSlotId = (int)(getCurrentTimeMillis() % 1000)/40;
				slotCtrl.freeSlot(lastSlotId);
			}
			
			if(currentFrameId() > lastFrameID) {
				if(Entrypoint.stationClass == 'A')
					calcFrameOffset();
				
				if(Entrypoint.stationClass == 'B')
					calcOffsetClassB();
				
				showFrameInfo();
				lastFrameID = currentFrameId();
				slotCtrl.nextFrame();
				
				try {
					if(dis.available() >= Message.DATALENGTH) {
						
						/* Enough data available */
						dis.readFully(data, 0, Message.DATALENGTH);
						transmitter.setMessage(new Message(Entrypoint.stationClass, data, -1, null));
						transmitter.gotData = true;
					} else {
						
						/* Not enough data available */
						// System.out.println("Warning: No new Data available! Sending old Data.");
					}
				} catch (IOException e) {
					System.out.println("ERROR: Reading data from System.in.");
					e.printStackTrace();
					try {
						
						System.out.println("Closing Streams.");
						
						/* Close Streams */
						dis.close();
						bis.close();
						
						transmitter.running = false;
						receiver.running = false;
						
						receiver.interrupt();
						
						return;
					} catch (IOException ex) {
						System.out.println("ERROR: closing stream.");
						ex.printStackTrace();
					}	
				}
			}
			
			try {
				
				/* Slow down process, too hacky... */
				sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public long getCurrentTimeMillis() {
		return (System.currentTimeMillis() + offset) - frameOffset;
	}
	
	public void setOffset(long millis) {
		this.offset = millis;
	}
	
	public void calcFrameOffset() {
		
		/* Possible 0 Class A Messages */
		if(classAMsg == 0) return;
		
		frameOffset += (long)(concOffset / classAMsg);
		concOffset = 0;
		classAMsg = 0;
	}
	
	public void calcOffsetClassB() {
		
		/* Possible 0 Class A Messages */
		if(classAMsg == 0) return;
		
		frameOffset += (long)concOffsetClassB / classAMsg;
		concOffsetClassB = 0;
		classAMsg = 0;
	}
	
	private void showFrameInfo() {
		//System.out.println("### FRAME: " + lastFrameID + " OFFSET: " + offset + "ms + " + frameOffset + "ms ##########");
		//for(int i = 0; i < 25; i++) {
			//if(i < 10) //System.out.print("0" + i + "|"); else System.out.print(i + "|");		
		//}
		//System.out.print('\n');
		
		for(int i = 0; i < 25; i++) {
			int cnt = slotCtrl.currentSlots[i];
			
			if(cnt == 0) {
				
				/* Noone sends on the Slot */
				//System.out.print(" O|");
			} else if(cnt == 1) {
				
				/* Someone sends on the Slot */
				//System.out.print(" X|");
			} else if(cnt > 1) {
				
				/* Collision on the Slot */
				//System.out.print("##|");
			} 
		}
		
		//System.out.print("\n\n");
	}
	
	private long currentFrameId() {
		return (long)getCurrentTimeMillis() / 1000;
	}
	
	private void handleMessage(Message msg) {
		byte[] msgData = msg.getMessage();
		
		/* RAW data *byte* */
		byte   stationClass_raw = msgData[0];
		byte[] usedata_raw      = Arrays.copyOfRange(msgData, 1, Message.DATALENGTH + 1);
		byte   slotid_raw       = msgData[25];
		byte[] millitime_raw    = Arrays.copyOfRange(msgData, 26, 34);
		
		/* Converted Data part 1 */
		char stationClass = (char) stationClass_raw;
		String useData = new String(usedata_raw, Charset.forName("UTF-8"));
		int slotid = Integer.valueOf(slotid_raw);
		
		/* Converted Data part 2 *ugly...* */
		ByteBuffer bybuf = ByteBuffer.allocate(Long.BYTES);
		bybuf.put(millitime_raw, 0, millitime_raw.length);
		bybuf.position(0);
		long milliTime = bybuf.getLong();
		
		if(false) {
			
			/* Dump Message Disable this wenn running multiple! */
			System.out.println("MSG: [ " 
								+ stationClass + " | " 
								+ useData +      " | "
								+ slotid +       " | "
								+ milliTime
								+ " ]");
		}
		
		if(milliTime / 1000 < lastFrameID) {
			
			/* Timestamp in Massage is too old */
			// System.out.println("Receivec old Package.");
		} else {
			
			/* Message was valid, update Slottable */
			slotCtrl.occupySlot(slotid);
			
			if(stationClass == 'A') {
				long msgoff = getCurrentTimeMillis() - milliTime;
				
				if( transmitter.slotNr != slotid ) {
					/* Set offsets */
					// System.out.println("Offset: " + msgoff);
					concOffset += msgoff;
					
					if(Entrypoint.stationClass == 'B') {
						
						/* Add Offset for Class B */
						concOffsetClassB += msgoff;
					}
				}
				
				
				
				classAMsg++;
			}
		}
	} 
}
