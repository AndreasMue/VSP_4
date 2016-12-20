/**
 * @Project: VSP_4
 * @File:    Receiver.java
 * @Package: station
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    20.12.2016
 */
package station;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ArrayBlockingQueue;

import Entrypoint.Entrypoint;

public class Receiver extends Thread {
	private static final int QUEUESIZE = 50;
	
	private StationController sc;
	private ArrayBlockingQueue<Message>messages = new ArrayBlockingQueue<Message>(QUEUESIZE);
	
	private byte[] buffer = new byte[Message.MESSAGELENGTH];
	private MulticastSocket skt;
	
	public boolean running = true;
	
	public Receiver(StationController sc) {
		this.sc = sc;
		
		init();
	}

	private void init() {
		try {
			InetAddress group = InetAddress.getByName(Entrypoint.baseAddress);
			skt = new MulticastSocket(Entrypoint.baseport);
			
			/* Join our group to receive packages */
			skt.joinGroup(group);
			
			/* Not needed to set TTL here, but it's good practice to always set it */
			skt.setTimeToLive(0); 
			
			skt.setLoopbackMode(false);
		} catch (IOException e) {
			System.out.println("ERROR: Creating MultiCastSocket");
			e.printStackTrace();
		}
	}
	
	private void handleData(byte[] data, InetAddress addr) {
		try {
			messages.put(new Message(data, addr));
			
			if(messages.remainingCapacity() == 0) {
				System.out.println("Warning: Queue is full!");
			}
		} catch (InterruptedException e) {
			System.out.println("ERROR: Adding Message to Queue.");
			e.printStackTrace();
		}
	}
	
	private void handlePkt(DatagramPacket pkt) {
		if(pkt.getLength() == Message.MESSAGELENGTH) {
			handleData(pkt.getData(), pkt.getAddress());
		} else {
			System.out.println("Warning: Data Length exceeds Message length! ("
							   + pkt.getLength() + " != " + Message.MESSAGELENGTH + ")");
		}
	}
	
	@Override
	public void run() {
		DatagramPacket pkt;
		
		System.out.println("Now Listening: " + skt.getLocalAddress()
						   + ":" + skt.getLocalPort());
		
		while(running) {
			pkt = new DatagramPacket(buffer, buffer.length);
			try {
				skt.receive(pkt);
				handlePkt(pkt);
			} catch (IOException e) {
				System.out.println("ERROR: Receiving packet.");
				e.printStackTrace();
			}
		}
	}
	
	public Message getMessage() throws InterruptedException {
		return messages.poll();
	}
}
