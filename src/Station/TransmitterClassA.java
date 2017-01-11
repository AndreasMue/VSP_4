/**
 * @Project: VSP_4
 * @File:    Station.java
 * @Package: Station
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    17.12.2016
 */
package Station;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;

import Entrypoint.Entrypoint;

public class TransmitterClassA extends StationBase{
	private static final int FRAMELENGTH = 1000;
	private static final int SLOTCOUNT = 25;
	
	/*
	 * AGGRESIVE allows the transmitter to test on multiple
	 * Slots over one Frame.
	 * 
	 * WARNING:
	 * This can result in testing up to SLOTCOUNT slots in one Frame
	 * and can clog up the System for other Transmitters as technically
	 * all Slots were occupied this Frame!!!
	 */
	private static final boolean AGGRESIVE = false;
	
	public enum SLOTSTATE {
		NOSLOT,
		RANDOMTAKEN,
		RANDOMTAKENSEND,
		TAKEN
	}
	
	public int slotNr = 0;
	public SLOTSTATE slotstate = SLOTSTATE.NOSLOT;
	
	
	private int slotlength;
	private int timeUntilSend;
	private int currentSlot;
	
	private InetAddress to;
	
	private MulticastSocket skt;
	
	
	public byte[] buffer;
	public Message msg;
	public boolean gotData = false;
	
	private SlotController sc;
	private StationController statCtrl;
	
	public boolean running = true;
	
	public TransmitterClassA(SlotController sc, StationController statCtrl) {
		this.sc = sc;
		this.statCtrl = statCtrl;
		
		slotlength = FRAMELENGTH / SLOTCOUNT;
		timeUntilSend = slotlength >> 1;
		
		try {
			to = InetAddress.getByName(Entrypoint.baseAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		init();
	}
	
	private void init() {
		try {
			
			
			buffer = new byte[Message.MESSAGELENGTH];
			
			NetworkInterface nic = NetworkInterface.getByName(Entrypoint.baseInterfaceName);
			InetAddress group = InetAddress.getByName(Entrypoint.baseAddress);
			InetSocketAddress socAdr = new InetSocketAddress(group, Entrypoint.baseport);
			
			skt = new MulticastSocket(Entrypoint.baseport);
			
			skt.joinGroup(socAdr, nic);
			skt.setTimeToLive(1);
			skt.setLoopbackMode(false);
			skt.setNetworkInterface(nic);
			
		} catch (IOException e) {
			System.out.println("ERROR: Creating MultiCastSocket");
			e.printStackTrace();
		}
	}
	
	public void setData(byte[] data) {
		if(data.length != Message.MESSAGELENGTH) {
			System.out.println("Warning: Data is too long. ( " + data.length + " != " + Message.MESSAGELENGTH + " )");
		} else {
			buffer = Arrays.copyOf(data, data.length);
		}
	}
	
	public void setMessage(Message msg) {
		this.msg = msg;
	}
	
	@Override
	public void run() {
		
		try {
			sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		while(running) {
			try {
				long timeUntilNext = nextSend();
				if(timeUntilNext > 0) sleep(timeUntilNext);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private long nextSend() throws InterruptedException {
		if(!gotData) return 1000L;
		
		long startCheck = statCtrl.getCurrentTimeMillis();
		long endCheck;
		long timeLeft;
		
		if(slotstate == SLOTSTATE.NOSLOT) {

			/* Got no Slot */
			slotNr = randomGetSlot();
			currentSlot = getCurrentSlot();
			return timeUntilNextSlot();
		} //else if(slotstate == SLOTSTATE.RANDOMTAKEN && sc.hasSlotCollision(slotNr)) {
//			
//			/* Got a collision after try slot take */
//			// System.out.println("RANDOMTAKEN | Co");
//			slotNr = randomGetSlot();
//			currentSlot = getCurrentSlot();
//			return timeUntilNextSlot();
//		} else if(slotstate == SLOTSTATE.RANDOMTAKEN && !sc.hasSlotCollision(slotNr)) {
//			
//			/* No collision after try slot take */
//			// System.out.println("RANDOMTAKEN | NoCo");
//			slotstate = SLOTSTATE.RANDOMTAKENSEND;
//		} else if(slotstate == SLOTSTATE.RANDOMTAKENSEND && !sc.hasSlotCollision(slotNr)) {
//			
//			/* No collision after try send on slot */
//			// System.out.println("RANDOMTAKENSEND | NoCo");
//			takeSlot();
//		} else if(slotstate == SLOTSTATE.RANDOMTAKENSEND && sc.hasSlotCollision(slotNr)) {
//			
//			/* Got a collision after try send on slot */
//			// System.out.println("RANDOMTAKENSEND | Co");
//			slotNr = randomGetSlot();
//			currentSlot = getCurrentSlot();
//			return timeUntilNextSlot();
//		} else if(slotstate == SLOTSTATE.TAKEN) {
//			
//			/* My Slot */
//			/* Happy */
//			
//		}
		
		
		endCheck = statCtrl.getCurrentTimeMillis();
		timeLeft = timeUntilSend - (endCheck - startCheck);
		
		/* wait until mid slot */
		sleep(timeLeft);
		
		slotNr = randomGetSlot();
		
		System.arraycopy(msg.getMessageNow(slotNr, statCtrl.getCurrentTimeMillis()), 0, buffer, 0, Message.MESSAGELENGTH);
		DatagramPacket pkt = new DatagramPacket(buffer, buffer.length, to, Entrypoint.baseport);
		try {
			skt.send(pkt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("Time: " + (System.currentTimeMillis()/1000) + ":" + (System.currentTimeMillis()%1000)%40);
		
		if(slotstate == SLOTSTATE.TAKEN) {
			
			/* We got our slot, wait for next slot in next frame */
			return (FRAMELENGTH - (statCtrl.getCurrentTimeMillis() % FRAMELENGTH)) + (slotNr * slotlength);
		} else if(AGGRESIVE){
			
			/* We got no save slot, check back in next slot */
			return slotlength - (statCtrl.getCurrentTimeMillis() - startCheck);
		} else {
			
			/* We got no slot, wait for next Frame */
			return (FRAMELENGTH - (statCtrl.getCurrentTimeMillis() % FRAMELENGTH)) + ((slotNr - 1) * slotlength);
		}
	}
	
	/*
	 * Calculate time until the next slot starts.
	 * Next slot varies based on slotstate.
	 */
	private long timeUntilNextSlot() {
		if(currentSlot > slotNr) {
			
			/* Slot in next Frame */
			int slotstowait = (SLOTCOUNT + slotNr) - currentSlot;
			return (slotstowait * slotlength) - ((statCtrl.getCurrentTimeMillis() % 1000) % 40);
		} else if(currentSlot < slotNr) {
			
			/* Slot in current Frame */
			int slotstowait = slotNr - currentSlot;
			return (slotstowait * slotlength) - ((statCtrl.getCurrentTimeMillis() % 1000) % 40);
		} else if(currentSlot == slotNr) {
			
			/* Slot is current Slot */
			return 0;
		} else {
			
			/* Undefined behavior, wait a Frame */
			return FRAMELENGTH;
		}
	}
	
	private int randomGetSlot() {
		slotstate = SLOTSTATE.RANDOMTAKEN;
		return sc.getFreeSlot();
	}
	
	private void takeSlot(){
		slotstate = SLOTSTATE.TAKEN;
	}
	
	private int getCurrentSlot(){
		
		/* Calculate the current slot based on time */
		int currentSlot = (int)(statCtrl.getCurrentTimeMillis() % FRAMELENGTH) / slotlength;
		return currentSlot;
	}
	
}
