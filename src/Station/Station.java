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

public class Station extends Thread{
	private final int framelength = 1000;
	private final int slotCount = 25;
	
	public enum SLOTSTATE {
		NOSLOT,
		RANDOMTAKEN,
		TAKEN
	}
	
	public int slotNr = 0;
	public SLOTSTATE slotstate = SLOTSTATE.NOSLOT;
	
	
	private int slotlength;
	private int timeUntilSend;
	private boolean gotCollision = false;
	private long nextFrameStart;
	private int currentSlot;
	
	private SlotController sc;
	
	public Station(SlotController sc) {
		this.sc = sc;
		slotlength = framelength / slotCount;
		timeUntilSend = slotlength >> 1;
	}
	
	@Override
	public void run() {
		
		/* Init next frame times */
		slotNr = randomGetSlot();
		nextFrameStart = ((System.currentTimeMillis() / framelength) * framelength) + framelength;
		
		/* Wait until slot time in next frame */
		try {
			sleep(nextFrameStart - System.currentTimeMillis());
			sleep(slotNr * slotlength);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int testiterations = 20;
		
		for(int i = 0; i < testiterations; i++){
			try {
				sleep(nextSend());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private long nextSend() throws InterruptedException{
		long startCheck = System.currentTimeMillis();
		long endCheck;
		long timeLeft;
		
		if(slotstate == SLOTSTATE.NOSLOT){

			/* Got no Slot */
			slotNr = randomGetSlot();
			currentSlot = getCurrentSlot();
			return timeUntilNextSlot();
		} else if(slotstate == SLOTSTATE.RANDOMTAKEN && gotCollision) {
			
			/* Got a collision after try slot take */
			slotNr = randomGetSlot();
			currentSlot = getCurrentSlot();
			return timeUntilNextSlot();
		} else if(slotstate == SLOTSTATE.RANDOMTAKEN && !gotCollision) {
			
			/* No collision after try slot take */
			takeSlot();
		} else if(slotstate == SLOTSTATE.TAKEN) {
			
			/* My Slot */
			/* Happy */
		}
		
		for(int i = 0; i < startCheck >> 16; i++) { startCheck++; startCheck--;/* doodle time prep */ }
		
		endCheck = System.currentTimeMillis();
		timeLeft = timeUntilSend - (endCheck - startCheck);
		
		/* wait until mid slot */
		sleep(timeLeft);
		
		System.out.println("Time: " + System.currentTimeMillis());
		
		for(int i = 0; i < endCheck >> 16; i++) { /* doodle time send */ }
		
		if(slotstate == SLOTSTATE.TAKEN) {
			
			/* We got our slot, wait for next slot in next frame */
			return framelength - (System.currentTimeMillis() - startCheck);
		}  else {
			
			/* We got no save slot, check back in next slot */
			return slotlength - (System.currentTimeMillis() - startCheck);
		}
	}
	
	/*
	 * Calculate time until the next slot starts.
	 * Next slot varies based on slotstate.
	 */
	private long timeUntilNextSlot(){
		if(currentSlot > slotNr) {
			int slotstowait = (slotCount + slotNr) - currentSlot;
			return slotstowait * slotlength;
		} else if(currentSlot < slotNr){
			int slotstowait = slotNr - currentSlot;
			return slotstowait * slotlength;
		} else if(currentSlot == slotNr) {
			return 0;
		} else {
			return framelength;
		}
	}
	
	private int randomGetSlot(){
		slotstate = SLOTSTATE.RANDOMTAKEN;
		if(slotNr < 15) gotCollision = true;
		else gotCollision = false;
		slotNr++;
		return (slotNr)%slotCount;
	}
	
	private void takeSlot(){
		slotstate = SLOTSTATE.TAKEN;
	}
	
	private int getCurrentSlot(){
		
		/* Calculate the current slot based on time */
		int currentSlot = (int)(System.currentTimeMillis() % framelength) / slotlength;
		System.out.println("CurrentSlot: " + currentSlot);
		return currentSlot;
	}
	
}
