/**
 * @Project: VSP_4
 * @File:    TestClass.java
 * @Package: test
 *
 * @Author:  Andreas Mueller (AndreasMue @ github.com)
 * @MatrNr:  209 918 2
 *
 * @Date:    17.12.2016
 */
package test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TestClass {
	public static void main (String[] args){
		BufferedInputStream bis = new BufferedInputStream(System.in);
		DataInputStream dis = new DataInputStream(bis);
		
		byte[] data = new byte[24];
		
		while(true){
			try {
				dis.readFully(data, 0, data.length);
				
				System.out.print("Data: ");
				for(int i = 0; i < data.length; i++) {
					System.out.print((char)data[i]);
				}
				System.out.print('\n');
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
