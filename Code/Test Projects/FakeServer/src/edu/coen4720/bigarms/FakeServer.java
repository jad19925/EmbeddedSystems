package edu.coen4720.bigarms;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FakeServer {
	public static final int SERVERPORT = 8080;
	
	public FakeServer() {
		
	}
	
	public void startThread(){
		new Thread(new ReceiveThread()).start();
	}
	
	public String getIpAddr() {
		//Get Wi-Fi information
	    String ip = "";
		try {
			ip = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //parse the IP Address into the correct format
	    String ipString = String.format(ip);

	    return ipString;
	}
	
	class ReceiveThread implements Runnable {

		private DatagramSocket receiveSocket;
		
		public ReceiveThread() {
			try {
				receiveSocket = new DatagramSocket(SERVERPORT);
				System.out.println("IP Address is: "+ getIpAddr() + "\n" + "Port Number: " + SERVERPORT + "\n");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {

				try {
					String read;
					
					byte[] data = new byte[1024];
					DatagramPacket dPack = new DatagramPacket(data,1024);
					receiveSocket.receive(dPack);
					read = new String(dPack.getData());
					
					System.out.println(read);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
