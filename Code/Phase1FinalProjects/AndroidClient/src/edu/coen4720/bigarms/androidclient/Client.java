/*
 * 
 * Author: 			Jerrell Jones
 * File Name:		Client.java
 * Last Updated: 	September 24, 2013
 * References:  	Wikipedia Client-Server Article
 * 					JavaCodeGeeks Client-Server Java
 * Description:		GUI Interface that sends strings to another android device (Server).
 * 					Implements the Client-Server framework. 
 * Updates:			Comments
 * 
 */
package edu.coen4720.bigarms.androidclient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class Client extends Activity {

	private Socket socket;
	
	public TextView text;

	private static final int SERVERPORT = 8080;//Open Port on Android Devices
	//All incoming Wi-Fi data comes through Port 8080 on the Android Device
	private String SERVER_IP;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		text = (TextView) findViewById(R.id.text1);		
	}

	public void onClick(View view) {
		try {
			EditText et = (EditText) findViewById(R.id.EditText01);
			String str = et.getText().toString();
			EditText ip = (EditText) findViewById(R.id.IPField);
			String test = ip.getText().toString();
			//All three lines are probably unnecessary will test later
			SERVER_IP = test;
			text.setText(SERVER_IP);
			SERVER_IP = text.getText().toString();
			//When send button is pressed, start thread with Server IP that was entered.
			new Thread(new ClientThread()).start();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
			out.println(str);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
				socket = new Socket(serverAddr, SERVERPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}
}