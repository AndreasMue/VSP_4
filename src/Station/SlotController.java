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

public class SlotController {
	private boolean slots[];
	
	public SlotController(int slotCount){
		slots = new boolean[slotCount];
		for(boolean slot : slots) {
			slot = true;
		}
	}
	
	public int getFreeSlot(){
		for(int i = 0; i < slots.length; i++) {
			if(slots[i]) return i;
		}
		
		return -1;
	}
	
	public void freeSlot(int slot){
		slots[slot] = true;
	}
	
	public void occupySlot(int slot){
		slots[slot] = false;
	}
}
