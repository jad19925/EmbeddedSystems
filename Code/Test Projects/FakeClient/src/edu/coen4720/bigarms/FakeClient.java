package edu.coen4720.bigarms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FakeClient {
	private DatagramSocket dSocket;
	private static final int SERVERPORT = 8080;//Open Port on Android Devices
	private static final int VEX_PORT = 9923;//
	//All incoming Wi-Fi data comes through Port 9923 on the Android Device
	private static final String SERVER_IP = "192.168.1.148";
	private static final String VEX_IP = "192.168.1.127";
	
	public FakeClient() {
		new Thread(new ClientThread()).start();
		
	}
	
	public void sendMessage(String string) {
		byte[] data = new byte[1024];
		DatagramPacket dPack = new DatagramPacket(data,1024);
		dPack.setData(string.getBytes());
		try {
			dSocket.send(dPack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress serverAddr = InetAddress.getByName(VEX_IP);

				dSocket = new DatagramSocket();
				dSocket.connect(InetAddress.getByName(VEX_IP), VEX_PORT);	

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}
}
