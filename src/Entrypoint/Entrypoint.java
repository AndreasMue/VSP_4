/**
 * @Project: VSP_4
 * @File:    Entrypoint.java
 * @Package: Entrypoint
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    17.12.2016
 */
package Entrypoint;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import Station.Station;
import Station.StationController;

public class Entrypoint {
	
	public static final String baseAddress = "225.10.1.2";
	public static final int baseport = 15010;
	
	public static void main(String[] args) throws SocketException {
		// Args in Order: IP, Port, Interface, StationID
		
		String address;
		int port;
		String interfaceName;
		int stationnumber;
		
		if(args.length > 0) {
			address = args[0];
		} else {
			address = baseAddress;
		}
		
		if(args.length > 1) {
			port = Integer.valueOf(args[1]);
		} else {
			port = baseport;
		}
		
		if(args.length > 2) {
			interfaceName = args[2];
		} else {
			Enumeration<NetworkInterface> netinterfaces = NetworkInterface.getNetworkInterfaces();
			if(netinterfaces.hasMoreElements()) {
				interfaceName = netinterfaces.nextElement().getName();
			} else {
				interfaceName = "ERROR";
			}
		}
		
		if(args.length == 4) {
			stationnumber = Integer.valueOf(args[3]);
		} else {
			stationnumber = -1;
		}
		
		System.out.println("### Starting Station ###");
		System.out.println("# PORT: " + port);
		System.out.println("# ADDR: " + address);
		System.out.println("# INTF: " + interfaceName);
		System.out.println("# STNR: " + stationnumber);
		System.out.println("########################");
		
		StationController stCtr = new StationController();
		stCtr.start();
		try {
			stCtr.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
