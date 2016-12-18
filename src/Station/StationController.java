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

public class StationController extends Thread {
	public boolean running = true;
	
	private Station station;
	private SlotController sc;
	
	public StationController() {
		sc = new SlotController(25);
		station = new Station(sc);
		station.start();
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				sleep(readData()); // ??
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private long readData(){
		System.out.println("ReadData");
		return 20;
	}
	
	private void execData(byte[] data){
		
	}
}
