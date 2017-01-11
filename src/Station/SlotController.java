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
package Station;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class SlotController {
	private int lastSlots[];
	public int currentSlots[];
	public int orderSlots[];
	
	private AtomicIntegerArray slotlist;
	
	private int lastSlot;
	
	private int freeSlots = 0;
	
	public SlotController(int slotCount){
		lastSlots = new int[slotCount];
		currentSlots = new int[slotCount];
		orderSlots = new int[slotCount];
		
		
		slotlist = new AtomicIntegerArray(slotCount);
		
		
		freeSlots = slotCount;
		
		for(int i = 0; i < lastSlots.length; i++) {
			lastSlots[i] = 0;
		}
		
		for(int i = 0; i < currentSlots.length; i++) {
			currentSlots[i] = 0;
		}
	}
	
	public int getFreeSlot(){
		Random rnd = new Random();
		int num = freeSlots > 0 ? rnd.nextInt(freeSlots) : 0;
		int idx = 0;
		
		for(int i = 0; i < lastSlots.length; i++) {
			if(slotlist.get(i) == 0) {
				if(idx == num) {
					return i + 1;
				} else {
					idx++;
				}
			}
		}
		
		for(int i = 0; i < lastSlots.length; i++) {
			if(lastSlots[i] == 0) return i + 1;
		}
		
		return -1;
	}
	
	
	public void freeSlot(int slot){
		slotlist.set(slot -1, 0);
	}
	
	public void occupySlot(int slot){
		if(slotlist.get(slot - 1) == 0) {
			freeSlots--;
		}
		
		slotlist.set(slot -1, slotlist.get(slot - 1) + 1);
	}
	
	public boolean hasSlotCollision(int slotId) {
		return lastSlots[slotId - 1] > 1 || slotlist.get(slotId - 1) > 1 ? true : false;
	}
	
	public void nextFrame() {
		lastSlots = Arrays.copyOf(currentSlots, currentSlots.length);
		freeSlots = currentSlots.length;
		
		for(int i = 0; i < currentSlots.length; i++) {
			slotlist.set(i, 0);
		}
	}
}
