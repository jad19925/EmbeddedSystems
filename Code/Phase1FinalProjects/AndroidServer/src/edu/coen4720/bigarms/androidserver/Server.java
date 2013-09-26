/*
 * 
 * Author: 			Jerrell Jones
 * File Name:		Server.java
 * Last Updated: 	September 24, 2013
 * References:  	Wikipedia Client-Server Article
 * 					JavaCodeGeeks Client-Server Java
 * Description:		GUI Interface that receives strings from another android device (Client).
 * 					Implements the Client-Server framework. 
 * Updates:			Comments
 * 
 */
package edu.coen4720.bigarms.androidserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import edu.coen4720.bigarms.Server.R;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class Server extends Activity {

	private ServerSocket serverSocket;

	Handler updateConversationHandler;

	Thread serverThread = null;

	private TextView text;

	public static final int SERVERPORT = 8080;

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

		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();

	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			text.setText(text.getText().toString()+"IP Address is: "+ getIpAddr() + "\n" + "Port Number: " + SERVERPORT + "\n");

			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					socket = serverSocket.accept();

					CommunicationThread commThread = new CommunicationThread(socket);
					new Thread(commThread).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class CommunicationThread implements Runnable {

		private Socket clientSocket;

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {
				//input stream from Client 
				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {

				try {
					//read input from client
					String read = input.readLine();
					
					//Display input from client
					updateConversationHandler.post(new updateUIThread(read));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			
			text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
		}
	}
}