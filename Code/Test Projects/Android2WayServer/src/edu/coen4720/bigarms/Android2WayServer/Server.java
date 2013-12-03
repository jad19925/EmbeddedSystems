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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.coen4720.bigarms.Android2WayServer.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Server extends Activity implements LocationListener, SensorEventListener {
	//network class values
	private DatagramSocket outSocket;
	private DatagramSocket tabletSocket;
	Handler updateConversationHandler;
	Thread serverThread = null;
	private TextView text;
	public static final int SERVERPORT = 8080;
	public static final int VEXPORT = 9923;
	public static final int TABLETPORT = 7777;
	private static final String VEX_IP = "192.168.1.127";
	private static final String TABLET_IP = "192.168.1.132";
	
	//gps class values
	private TextView latitudeField;
	private TextView longitudeField;
	private TextView toDestBField;
	private TextView toDestDField;
	private TextView tvFacing;
	private ScrollView messageScroll;
	private LocationManager locationManager;
	private String provider;
	private final String networkProvider = LocationManager.NETWORK_PROVIDER;
	private SensorManager mSensorManager;
	private float[] accValues = null;
	private float[] magValues = null;
	
	private double latitude = 43.037826;
	private double longitude = -87.930847;
	private double facing;
	private double destLat = 43.036969;
	private double destLon = -87.929579;
	private double destBearing = 0;
	private double destDistance = 0;
	private double prevBearing = 0;
	private double prevDistance = 0;
	private String lastCommand = "null";
	private boolean autoMove = false;
	private boolean manMove = false;
	
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
		latitudeField = (TextView) findViewById(R.id.TextView02);
	    longitudeField = (TextView) findViewById(R.id.TextView04);
	    toDestBField = (TextView) findViewById(R.id.TextView06);
	    toDestDField = (TextView) findViewById(R.id.TextView08);
	    messageScroll = (ScrollView) findViewById(R.id.scrollViewMessages);
		
		//Handler that receives information from Client
		updateConversationHandler = new Handler();

		new Thread(new CommunicationThread()).start();
		new Thread(new ClientThread()).start();
		new Thread(new TabletThread()).start();
		
		//gps code
		// Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the location provider -> use default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);
	    
	    // TextView that will tell the user what degree is he heading
		tvFacing = (TextView) findViewById(R.id.tvFacing);
		// initialize your android device sensor capabilities
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

	    // Initialize the location fields
	    if (location != null) {
	      System.out.println("Provider " + provider + " has been selected.");
	      onLocationChanged(location);
	    } else {
	      latitudeField.setText("Location not available");
	      longitudeField.setText("Location not available");
	    }
	}

	class CommunicationThread implements Runnable {

		private DatagramSocket receiveSocket;
		
		public CommunicationThread() {

		}

		public void run() {
			try {
				receiveSocket = new DatagramSocket(SERVERPORT);
				text.setText(text.getText().toString()+"IP Address is: "+ getIpAddr() + "\n" + "Port Number: " + SERVERPORT + "\n");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	class TabletThread implements Runnable {
		@Override
		public void run() {

			try {
				tabletSocket = new DatagramSocket();
				tabletSocket.connect(InetAddress.getByName(TABLET_IP), TABLETPORT);

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
			messageScroll.fullScroll(View.FOCUS_DOWN);
			boolean send = false;
			byte[] data = new byte[1024];
			DatagramPacket dPack = new DatagramPacket(data,1024);
			//do processing here. currently just forwards the command to the vex, but we can do calls to gps code later
			if(msg.startsWith("forward") || msg.startsWith("backward") || msg.startsWith("left") || msg.startsWith("right") ||
					msg.startsWith("stop") || msg.startsWith("speed") || msg.startsWith("exit") || msg.startsWith("move")){
				dPack.setData(msg.getBytes());
				send = true;
				//set class message to stop auto-movement to waypoint
				manMove = true;
				autoMove = false;
			}
			else if(msg.startsWith("lat")) {
				//set class variables to stop moving while message is being parsed
				autoMove = false;
				manMove = false;
				//parse message, set parameters based on contents
				//lat/long string format = "lat%flon%f"
				String dString = msg.substring(3, msg.lastIndexOf('l'));
				destLat = Double.parseDouble(dString);
				dString = msg.substring(msg.lastIndexOf('n') + 1);
				destLon = Double.parseDouble(dString);
				text.setText(text.getText().toString() + "New Waypoint: "+ Double.toString(destLat) + ", " +
						Double.toString(destLon) + "\n");
				//set class message to restart movement
				new Thread(new NavigationThread()).start();
				autoMove = true;
				
			}
			else{
				//do some other processing yet to be determined
				dPack.setData(new String("not a command").getBytes());
				text.setText(text.getText().toString()+"Invalid Command\n");
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
	
	class NavigationThread implements Runnable {

		double prevBearingDifference = 0;
		@Override
		public void run() {
			int offset = 1750;//inside = 1700 outside = 1750
			int divider = 22;//inside = 41, outside = 22
			//stop
			String msg;
			byte[] data = new byte[1024];
			DatagramPacket dPack = new DatagramPacket(data,1024);
			msg = "stop";
			dPack.setData(msg.getBytes());
			try {
				outSocket.send(dPack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//sleep 0.1 mSec
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//calculate new bearing to destination (won't be calculated while not moving)
			double[] bearing = {0, 0};
			getBearingToDest(latitude, longitude, destLat, destLon, bearing);
			destBearing = bearing[0];
			destDistance = bearing[1];
			
			//guess turn towards destination based on previous bearing
//			int bearingDiff = 0;
			int leftDegrees = (int)(prevBearing - destBearing + 360) % 360;
			if(leftDegrees < 180) {
				//turn left
//				msg = "moveR100L-100";
//				dPack.setData(msg.getBytes());
//				bearingDiff = leftDegrees;
				turnDegrees(leftDegrees,true);
			}
			else {
				//turn right
//				msg = "moveR-100L100";
//				dPack.setData(msg.getBytes());
//				bearingDiff = 360 - leftDegrees;
				turnDegrees((360-leftDegrees),false);
			}
//			try {
//				outSocket.send(dPack);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			//set stop data
//			msg = "stop";
//			dPack.setData(msg.getBytes());
//			//sleep x? seconds
//			try {
//				Thread.sleep(offset + (1000*bearingDiff)/divider);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			//send stop
//			try {
//				outSocket.send(dPack);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			final int counterLimit = 20;
			int counter = 0;
			int insideSpeed = 100;
			//start moving toward the destination correcting as we go
			//start moving forward before entering the loop
			
			//movement loop
			//text.setText(text.getText().toString()+"Start Movement Loop\n");
			while(autoMove && !Thread.currentThread().isInterrupted()) {
				if(counter >= counterLimit) {
					counter = 0;
					if(prevDistance < destDistance) {
						//going the wrong way
						//turn 180 and go forward
						turnDegrees(180,true);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						msg = "moveR100L100";
						dPack.setData(msg.getBytes());
						try {
							outSocket.send(dPack);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						if(prevBearing < destBearing) {
							//current heading is left of where we need to go, adjust right
							if(Math.abs(prevBearingDifference) < Math.abs(prevBearing - destBearing)) {
								//getting closer
								insideSpeed = insideSpeed + (100-insideSpeed)/2;
							}
							else {
								//getting farther
								insideSpeed = (9 * insideSpeed)/10;
							}
							msg = "moveR" + insideSpeed + "L100";
							prevBearingDifference = prevBearing - destBearing;
						}
						else if(prevBearing > destBearing) {
							//current heading is right of where we need to go, adjust left
							if(Math.abs(prevBearingDifference) < Math.abs(prevBearing - destBearing)) {
								//getting closer
								insideSpeed = insideSpeed + (100-insideSpeed)/2;
							}
							else {
								//getting farther
								insideSpeed = (9 * insideSpeed)/10;
							}
							msg = "moveR100L" + insideSpeed;
							prevBearingDifference = prevBearing - destBearing;
						}
						else {
							//prev and destination bearing are the same, move straight
							msg = "moveR100L100";
							prevBearingDifference = prevBearing - destBearing;
						}
						dPack.setData(msg.getBytes());
						try {
							outSocket.send(dPack);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					prevBearing = destBearing;
					prevDistance = destDistance;
				}
				else {
					counter++;
				}
				//maybe do this assignment only when we actually are making move commands?
//				prevBearing = destBearing;
//				prevDistance = destDistance;
			}
		}
		
		public void turnDegrees(int degrees, boolean left) {
			String msg;
			byte[] data = new byte[1024];
			DatagramPacket dPack = new DatagramPacket(data,1024);
			final int offset = 1750;//inside = 1700 outside = 1750
			final int divider = 22;//inside = 41, outside = 22
			
			if(left) {
				//turn left
				msg = "moveR100L-100";
				dPack.setData(msg.getBytes());
			}
			else {
				//turn right
				msg = "moveR-100L100";
				dPack.setData(msg.getBytes());
			}
			try {
				outSocket.send(dPack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//set stop data
			msg = "stop";
			dPack.setData(msg.getBytes());
			//sleep x? seconds
			try {
				Thread.sleep(offset + (1000*degrees)/divider);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//send stop
			try {
				outSocket.send(dPack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
	    locationManager.requestLocationUpdates(provider, 1, 1, this);
	    locationManager.requestLocationUpdates(networkProvider, 0, 0, this);
	    // for the system's orientation sensor registered listeners
	    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	  				SensorManager.SENSOR_DELAY_NORMAL);
	    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
  				SensorManager.SENSOR_DELAY_NORMAL);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		  super.onPause();
	    locationManager.removeUpdates(this);
	    // to stop the listener and save battery
	    mSensorManager.unregisterListener(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = (location.getLatitude());
		double lng = (location.getLongitude());
		latitude = lat;
		longitude = lng;
		latitudeField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
		double[] bearing = {0, 0};
		getBearingToDest(lat, lng, destLat, destLon, bearing);
		destBearing = bearing[0];
		destDistance = bearing[1];
		toDestBField.setText(String.valueOf(bearing[0]));
		toDestDField.setText(String.valueOf(bearing[1]));
		
		//send location back to the tablet
		String msg;
		byte[] data = new byte[1024];
		DatagramPacket dPack = new DatagramPacket(data,1024);
		msg = "lat" + String.valueOf(lat) + "lon" + String.valueOf(lng);
		dPack.setData(msg.getBytes());
		try {
			tabletSocket.send(dPack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	        Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
	    Toast.makeText(this, "Disabled provider " + provider,
	        Toast.LENGTH_SHORT).show();
	}
	  
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] R = new float[9];
		float[] I = new float[9];
		float[] orientation = new float[3];
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accValues = event.values;
		}
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magValues = event.values;
		}
		
		if(accValues != null && magValues != null) {
			SensorManager.getRotationMatrix(R, I, accValues, magValues);
			SensorManager.getOrientation(R, orientation);
			facing = Math.round(Math.toDegrees(orientation[0]));
			if(facing < 0) {
				facing = facing + 360.0;
			}
			tvFacing.setText("Facing (Degrees): " + Double.toString(facing) + "Deg");
		}
		
		//send control messages
//		boolean send = false;
//		String msg;
//		byte[] data = new byte[1024];
//		DatagramPacket dPack = new DatagramPacket(data,1024);
//		
//		if(manMove) {
//			//do nothing, gui is in control
//			//System.out.println("manual movement");
//		}
//		else if (autoMove) {
//			//movement algorithm for moving to waypoint
//			//System.out.println("doing automove");
//			if(destDistance > 3){
//				if(facing - destBearing > 3){
//					//set msg to turn left
//					msg = "left";
//					dPack.setData(msg.getBytes());
//					send = !lastCommand.equals("left");
//					lastCommand = "left";
//				}
//				else if (destBearing - facing > 3){
//					//set msg turn right
//					msg = "right";
//					dPack.setData(msg.getBytes());
//					send = !lastCommand.equals("right");
//					lastCommand = "right";
//				}
//				else {
//					if(lastCommand.equals("left") || lastCommand.equals("right")) {
//						msg = "stop";
//						lastCommand = "stop";
//						send = true;
//					}
//					else if(lastCommand.equals("stop")) {
//						msg = "forward";
//						lastCommand = "forward";
//						send = true;
//					}
//					else{
//						msg = "stop";
//						send = false;
//					}
//					dPack.setData(msg.getBytes());
//				}
//			}
//			else {
//				//stop when we reach the destination
//				msg = "stop";
//				dPack.setData(msg.getBytes());
//				send = !lastCommand.equals("stop");
//				lastCommand = "stop";
//			}
//		}
//		else {
//			//send stop messages
//			msg = "stop";
//			dPack.setData(msg.getBytes());
//			send = !lastCommand.equals("stop");
//			lastCommand = "stop";
//		}
//		
//		if(send){
//			try {
//				outSocket.send(dPack);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not in use
	}
	  
	public void getBearingToDest(double startLat, double startLon, double endLat, double endLon, double[] bearing) {
		startLat = Math.toRadians(startLat);
		startLon = Math.toRadians(startLon);
		endLat = Math.toRadians(endLat);
		endLon = Math.toRadians(endLon);
		final double R = 6371000; //radius of earth in meters
		  
		double dLon = endLon - startLon;
		double dPhi = Math.log(Math.tan(endLat/2.0+Math.PI/4.0)/Math.tan(startLat/2.0+Math.PI/4.0));
		if(Math.abs(dLon) > Math.PI) {
			if(dLon > 0.0) {
				dLon = -(2.0 * Math.PI - dLon);
			}
			else {
				dLon = (2.0 * Math.PI - dLon);
			}
		}
		  
		bearing[0] = (Math.toDegrees(Math.atan2(dLon, dPhi)) + 360.0) % 360.0;
		
		double dLat = endLat - startLat;
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				   Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(startLat) * Math.cos(endLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		bearing[1] = R * c;
	}
}