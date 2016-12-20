/**
 * @Project: VSP_4
 * @File:    SlotController.java
 * @Package: Station
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    17.12.2016
 */
package station;

import java.util.Arrays;

public class SlotController {
	private int lastSlots[];
	public int currentSlots[];
	
	public SlotController(int slotCount){
		lastSlots = new int[slotCount];
		currentSlots = new int[slotCount];
		
		
		for(int i = 0; i < lastSlots.length; i++) {
			lastSlots[i] = 0;
		}
		
		for(int i = 0; i < currentSlots.length; i++) {
			currentSlots[i] = 0;
		}
	}
	
	public int getFreeSlot(){
		for(int i = 0; i < lastSlots.length; i++) {
			if(lastSlots[i] == 0 && currentSlots[i] == 0) return i;
		}
		
		for(int i = 0; i < lastSlots.length; i++) {
			if(lastSlots[i] == 0) return i;
		}
		
		return -1;
	}
	
	public void freeSlot(int slot){
		currentSlots[slot] = 0;
	}
	
	public void occupySlot(int slot){
		currentSlots[slot]++;
	}
	
	public boolean hasSlotCollision(int slotId) {
		return lastSlots[slotId] > 1 || currentSlots[slotId] > 1 ? true : false;
	}
	
	public void nextFrame() {
		lastSlots = Arrays.copyOf(currentSlots, currentSlots.length);
		
		for(int i = 0; i < currentSlots.length; i++) {
			currentSlots[i] = 0;
		}
	}
}
