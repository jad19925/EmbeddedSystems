package com.javacodegeeks.android.androidsocketclient;

/**
 * Team BigARMs, Project 1 Android GUI for COEN 4720 Embedded Systems Design.
 * The code in this project does not support Android 3.0 and below. 
 * Fall 2013.
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class Client extends Activity implements SpeedPickerFragment.mySpeedChangeListener {

	private Socket socket;
	private DatagramSocket dSocket;
	private static final boolean UDP = true;

	private static final int SERVERPORT = 9923;//Open Port on Android Devices
	//All incoming Wi-Fi data comes through Port 9923 on the Android Device
	private static final String SERVER_IP = "192.168.1.127";//"192.168.1.127";
	
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
	    newFragment.show(getFragmentManager(), "speedPicker");
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		SpeedPickerFragment myFragment = (SpeedPickerFragment) dialog;
		String str = myFragment.speed_str;
		
		currentSpeed = myFragment.speed;
		
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




