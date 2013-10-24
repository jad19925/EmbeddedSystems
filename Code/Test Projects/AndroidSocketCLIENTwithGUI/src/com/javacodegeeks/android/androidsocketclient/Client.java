package com.javacodegeeks.android.androidsocketclient;

/**
 * Team BigARMs, Project 1 Android GUI for COEN 4720 Embedded Systems Design.
 * The code in this project does not support Android 3.0 and below. 
 * Fall 2013.
 * References used:
 * http://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
 * http://developer.android.com/design/building-blocks/pickers.html
 * http://developer.android.com/guide/topics/ui/dialogs.html#PassingEvents
 * 
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Client extends Activity implements SpeedPickerFragment.mySpeedChangeListener {

	private Socket socket;
	private DatagramSocket dSocket;
	private static final boolean UDP = true;

	private static final int SERVERPORT = 9923;//8080;//9923;//Open Port on Android Devices
	//All incoming Wi-Fi data comes through Port 9923 on the Android Device
	private static final String SERVER_IP = "192.168.1.127"; //"192.168.1.148";//
	
	public int currentSpeed = 15; //default speed 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		

		new Thread(new ClientThread()).start();
	}

	public void onClick(View view) {
		try {
			
			//get String from GUI:
			Button pressedButton = (Button) findViewById(view.getId());
			String str = pressedButton.getText().toString();
			
			
			
			if(!UDP) {
				PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
				out.println(str);
			}
			else {
				byte[] data = new byte[1024];
				DatagramPacket dPack = new DatagramPacket(data,1024);
				dPack.setData(str.getBytes());
				dSocket.send(dPack);
			}
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

				if(!UDP) {
					socket = new Socket(serverAddr, SERVERPORT);
				}
				else {
					dSocket = new DatagramSocket();
					dSocket.connect(InetAddress.getByName(SERVER_IP), SERVERPORT);
				}

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	} //end class ClientThread
	
	
	
	
	///---------------------------------------------------------
	/// new methods for calling the Fragment GUI:
	public void showSpeedPickerDialog(View v) {
	    DialogFragment newFragment = new SpeedPickerFragment();
	    newFragment.show(getFragmentManager(), "speedPicker");  //shows the speed changing dialog box
	}

	//if the user clicks the 'ChangeSpeed' button on the dialog box
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		SpeedPickerFragment myFragment = (SpeedPickerFragment) dialog;
		String str = myFragment.speed_str;  //get speed string from the Dialog box
		
		currentSpeed = myFragment.speed;  //update persistent currentSpeed variable.
		
		//send UDP:
				
try {
			
			if(!UDP) {
				PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())),
					true);
				out.println(str);
			}
			else {
				byte[] data = new byte[1024];
				DatagramPacket dPack = new DatagramPacket(data,1024);
				dPack.setData(str.getBytes());
				dSocket.send(dPack);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// Auto-generated method stub
		//do nothing on cancel
		
	}
	
	
	
	
}// end class Client




