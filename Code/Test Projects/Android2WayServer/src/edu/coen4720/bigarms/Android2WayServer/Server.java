/*
 * 
 * Author: 			Jacob Dahleen
 * File Name:		Server.java
 * Last Updated: 	October 4, 2013
 * References:  	Wikipedia Client-Server Article
 * 					JavaCodeGeeks Client-Server Java
 * Description:		GUI Interface that receives strings from another android device (Client) and .
 * 					Implements the Client-Server framework. 
 * Updates:			Comments
 * 
 */
package edu.coen4720.bigarms.Android2WayServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import edu.coen4720.bigarms.Android2WayServer.R;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class Server extends Activity {
	
	private DatagramSocket outSocket;

	Handler updateConversationHandler;

	Thread serverThread = null;

	private TextView text;

	public static final int SERVERPORT = 8080;
	
	public static final int VEXPORT = 9923;
	private static final String VEX_IP = "192.168.1.127";
	
	private static final boolean UDP = true;

	public String getIpAddr() {
		//Get Wi-Fi information
	    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	    int ip = wifiInfo.getIpAddress();
	    
	    //parse the IP Address into the correct format
	    String ipString = String.format(
	    "%d.%d.%d.%d",
	    (ip & 0xff),
	    (ip >> 8 & 0xff),
	    (ip >> 16 & 0xff),
	    (ip >> 24 & 0xff));

	    return ipString;
	}
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Text Field
		text = (TextView) findViewById(R.id.text2);
		
		//Handler that receives information from Client
		updateConversationHandler = new Handler();

		new Thread(new CommunicationThread()).start();
		new Thread(new ClientThread()).start();
	}

	class CommunicationThread implements Runnable {

		private BufferedReader input;
		
		private DatagramSocket receiveSocket;
		
		public CommunicationThread() {
			try {
				receiveSocket = new DatagramSocket(SERVERPORT);
				text.setText(text.getText().toString()+"IP Address is: "+ getIpAddr() + "\n" + "Port Number: " + SERVERPORT + "\n");
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
					
					//Display input from client
					updateConversationHandler.post(new ProcessMessageThread(read));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				outSocket = new DatagramSocket();
				outSocket.connect(InetAddress.getByName(VEX_IP), VEXPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	class ProcessMessageThread implements Runnable {
		private String msg;

		public ProcessMessageThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
			boolean send = false;
			byte[] data = new byte[1024];
			DatagramPacket dPack = new DatagramPacket(data,1024);
			//do processing here. currently just forwards the command to the vex, but we can do calls to gps code later
			if(msg.startsWith("forward") || msg.startsWith("backward") || msg.startsWith("left") || msg.startsWith("right") ||
					msg.startsWith("stop") || msg.startsWith("speed") || msg.startsWith("exit")){
				dPack.setData(msg.getBytes());
				send = true;
			}
			else{
				//do some other processing yet to be determined
				dPack.setData(new String("not a command").getBytes());
				text.setText(text.getText().toString()+"Command not sent\n");
			}
			
			if(send){
				try {
					outSocket.send(dPack);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}