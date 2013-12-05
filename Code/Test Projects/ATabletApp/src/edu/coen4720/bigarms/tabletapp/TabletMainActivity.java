/*Tablet Main Activity.java
 * Combines the Location related code and Server code from the Android2WayServer Project
 * and  
 * the Remote Control GUI and related code from the AndroidSocketClientwithGUISpeed Project
 * 
 * Author: SJK
 * Date: Oct 23, 2013
 */
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

package edu.coen4720.bigarms.tabletapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


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
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TabletMainActivity extends Activity implements LocationListener, SensorEventListener, 
						SpeedPickerFragment.mySpeedChangeListener, LatLonPickerFragment.WPSelectListener, 
						WayPointAdderFragment.WPAdderListener, ManualWayPointFragment.WPManualListener, OnMapLongClickListener  {
	//network class values
	private DatagramSocket outSocket;  
	Handler updateConversationHandler;
	Thread serverThread = null;
	private TextView text;
	public static final int PHONEPORT = 8080;
	public static final int TABLETPORT = 7777;
	public static final int VEXPORT = 9923;
	private static final String VEX_IP = "192.168.11.23";  // for dd-wrt: "192.168.1.127"
	private static final String PHONE_IP = "192.168.11.42"; // testing: should be"192.168.1.148";

	//gps class values
	private TextView latitudeField;
	private TextView longitudeField;
	//private TextView toDestBField;
	private TextView toDestDField;
	private TextView tvFacing;
	private LocationManager locationManager;
	private String provider;
	private SensorManager mSensorManager;
	private float[] accValues = null;
	private float[] magValues = null;
	private final String networkProvider = locationManager.NETWORK_PROVIDER;
	
	private double latitude = 0;  //default
	private double longitude = 0; //default
	private double facing;
	private  double destLat = 43.036969;
	private  double destLon = -87.929579;
	
	public String myLocation = "lat0.000000lon-0.000000";  //default value when no location info is available.
	public String mapPoint = "lat0.0000lon-0.00000"; //default value; a position on the map, that will be updated in onMapLongClick 
	
	public int currentSpeed = 15; //default speed 
	
	public ArrayList<String> waypointList;  //list of WayPoints
	public int waypointCounter = 0; //keeps a count of the current waypoint in the waypoint list.
	MapFragment myMapFragment; //for showing a Map on the screen
	GoogleMap myMap;  //map object itself - for adding markers to it
	Marker destMarker ; // a marker object for displaying a target destination
	Marker tabletMarker; //a marker object for displaying tablet's location
	Marker phoneMarker ; // a marker object for displaying the phone's current location.
	
	//integrating tilt control
	boolean tiltEnabled = false;  //off by default
	private TextView tiltInfoDisplay;
	private float[] gravity = new float[3];
	private int tiltcounter = 0;
	private final int counterLimit = 20; // default: 10. increase this number to make the tilt control send control messages more infrequently.
	
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
		setContentView(R.layout.activity_tablet_main);
		
		//Text Field
		text = (TextView) findViewById(R.id.text2);
		text.setText(" ");
		latitudeField = (TextView) findViewById(R.id.TextViewLatitudeValue);
	    longitudeField = (TextView) findViewById(R.id.TextViewLongitudeValue);
	    //toDestBField = (TextView) findViewById(R.id.TextViewBearingValue);
	    toDestDField = (TextView) findViewById(R.id.TextViewDistanceValue);
	    tiltInfoDisplay = (TextView) findViewById(R.id.TextViewTilt);
		
		//Handler that receives information from Client
		updateConversationHandler = new Handler();

		new Thread(new CommunicationThread()).start();
		new Thread(new ClientThread()).start();  //thread to communicate with VEX
		
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

	    //map object initialization to prevent null pointer error
	    myMap = null;
		
	    // Initialize the location fields
	    if (location != null) {
	      System.out.println("Provider " + provider + " has been selected.");
	      onLocationChanged(location);
	    } else {
	      latitudeField.setText("Not available");
	      longitudeField.setText("Not available");
	    }
	    
	    //initialize the WayPoint list
	    //Note: this list will be dynamically changed during execution to add more waypoints, 
	    //but those changes will not be persistent when the program is restarted.
	    waypointList = new ArrayList<String>();
	    /*waypointList.add("lat43.036969lon-87.929579");  //sample waypoints
	    waypointList.add("lat43.0384707lon-87.933528");
	    waypointList.add("lat43.037693lon-87.929883");
	    waypointList.add("lat43.037364lon-87.929759");
	    */
	    		waypointList.add("lat43.037784lon-87.930132");
	    		waypointList.add("lat43.037705lon-87.930132");
	    		waypointList.add("lat43.037638lon-87.930134");
	    		waypointList.add("lat43.037431lon-87.930121");
	    		waypointList.add("lat43.037368lon-87.930124");
	    		waypointList.add("lat43.037274lon-87.930129");
	    		waypointList.add("lat43.037252lon-87.930102");
	    		waypointList.add("lat43.037205lon-87.930027");
	    		waypointList.add("lat43.037144lon-87.929939");
	    		waypointList.add("lat43.037088lon-87.929837");
	    		waypointList.add("lat43.037023lon-87.929724");
	    		waypointList.add("lat43.03697lon-87.929614");
	    		waypointList.add("lat43.036956lon-87.929579");
	    		waypointList.add("lat43.036962lon-87.929582");  //(lalumiere)
	    		waypointList.add("lat43.03697lon-87.929547");
	    		waypointList.add("lat43.03699lon-87.929501");
	    		waypointList.add("lat43.037105lon-87.929391");
	    		waypointList.add("lat43.03737lon-87.929182");
	    		waypointList.add("lat43.037458lon-87.929086");
	    		waypointList.add("lat43.03747lon-87.929024");
	    		waypointList.add("lat43.03755lon-87.929021");
	    		waypointList.add("lat43.037666lon-87.929016");
	    		waypointList.add("lat43.037717lon-87.929016");
	    		waypointList.add("lat43.037723lon-87.929115");
	    		waypointList.add("lat43.037711lon-87.929421");
	    		waypointList.add("lat43.037705lon-87.929657");
	    		waypointList.add("lat43.037703lon-87.929896");
	    		waypointList.add("lat43.037705lon-87.930035");
	    		waypointList.add("lat43.037709lon-87.930124");
	    
	    //TODO: Add more default waylists
	    
	    //options to config map initially
	    LatLng initialmapcoords = new LatLng (43.037835,-87.930859);  //Olin southside
	    CameraPosition cp = new CameraPosition (initialmapcoords, 16.5f, 0.0f, 0.0f); //zoom, tilt, bearing TODO: proper floats
	    GoogleMapOptions options = new GoogleMapOptions();
	    options.mapType(GoogleMap.MAP_TYPE_HYBRID);
	    options.camera(cp);  //sets initial camera position
	   	    
	    //add a map to a portion of the Activity screen:
	    myMapFragment = MapFragment.newInstance(options);
	    	    
	    //add the map to the app
	    android.app.FragmentTransaction fragmentTransaction =
	            getFragmentManager().beginTransaction();
	    fragmentTransaction.add(R.id.Map_Layout, myMapFragment);
	    fragmentTransaction.commit();
	    

	    	    
	    
	} //end method onCreate
	
	
		
	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
	    locationManager.requestLocationUpdates(provider, 1, 1, this);
	    locationManager.requestLocationUpdates(networkProvider, 0, 0, this);  //get location info from wifi as well.
	    // for the system's orientation sensor registered listeners
	    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	  				SensorManager.SENSOR_DELAY_GAME);
	    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
  				SensorManager.SENSOR_DELAY_GAME);
	    //display ip address info again on resume.
	    text.setText(text.getText().toString() + "My IP Address is: "+ getIpAddr() + "\n" + "My Port Number: " + TABLETPORT + "\n");
	
	    
	    //add marker to map: only works if google play service is available
	 // Check that Google Play services is available
       int resultCode =
               GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
       if (ConnectionResult.SUCCESS == resultCode) {
	    myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.Map_Layout)).getMap();
	  
	    
	    //add markers to map
        destMarker =  myMap.addMarker(new MarkerOptions()
        .position(new LatLng (destLat, destLon)).title("Destination")); 
        
        tabletMarker = myMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
	
        phoneMarker =  myMap.addMarker(new MarkerOptions().position(new LatLng(destLat, destLon)).title("Phone").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        
       //change other map configurations
      myMap.setOnMapLongClickListener(this);
               
       }
       else {
   	    //initialize markers to prevent crashes
    	destMarker = new Marker (null);
   	    tabletMarker = new Marker (null);
   	    phoneMarker = new Marker (null);
    	   
       }
	    
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		  super.onPause();
	    locationManager.removeUpdates(this);
	    // to stop the listener and save battery
	    mSensorManager.unregisterListener(this);
	    
	    //remove markers from map so that they can be re-added upon resume
	    destMarker.remove();
	    tabletMarker.remove();
	    phoneMarker.remove();
	   
	    
	}

	/**
	 * When location has changed, update the GUI with the new location info.
	 * Automatically called, as this Activity implements the LocationListener
	 * See how the LocationManager was configured for update intervals
	 */
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
		//toDestBField.setText(String.valueOf(bearing[0]));
		
		toDestDField.setText(String.valueOf(bearing[1]).substring(0, 8) + " m"); //arbitrarily cut length
		
		myLocation = parseLocation(lat, lng); //update the myLocation string when location has changed
		//send control messages;
		
		//change the tablet marker on map: 
		if (myMap != null){
		tabletMarker.setPosition(new LatLng(lat, lng));
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
			//// Tilt Control enablers here!
			if (tiltEnabled){
				float x, y, z;
				int percentLeft = 0, percentRight = 0;
				
				final float alpha = 0.8f;
				x = gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
				y = gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
				z = gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			
				//calibrate for flatness on a moving earth
				x -= 0.065;
				y -= 0.133;
				
				//tilted all the way to the right
				if(Math.abs(x/y) > 40 && x > 0)
				{
					percentRight = 100;
					percentLeft = -100;
				}
				//tilted all the way left
				else if(Math.abs(x/y) > 40 && x < 0)
				{
					percentRight = -100;
					percentLeft = 100;
				}
				//moving zero
				else if(Math.abs(x) < 1 && Math.abs(y) < 1)
				{
					percentRight = 0;
					percentLeft = 0;
				}
				else
				{
					//percentRight = (int)maxAbs((x * 100 / 6), 100);
					//percentLeft = (int)maxAbs((-x * 100 / 6), 100);
					if(Math.abs(y) > Math.abs(x)) {
						float vF = (-y*100)/6;
						if(vF >= 0) {
							vF = Math.min(vF,100);
						}
						else{
							vF = Math.max(vF, -100);
						}
						if(x > 0) {
							percentRight = (int)vF;
							percentLeft = (int)((Math.abs(vF) - Math.min((x*100)/6, 100))*Math.signum(vF));
						}
						else {
							percentRight = (int)((Math.abs(vF) + Math.max((x*100)/6, -100))*Math.signum(vF));
							percentLeft = (int)vF;
						}
					}
					else {
						float speed = Math.max(Math.abs(x*100)/6, 100);
						int dir = (int)Math.signum(-y);
						if(x > 0) {
							percentRight = (int)speed*dir;
							percentLeft = (int)(Math.abs(speed) - Math.min((x*100)/6, 100))*dir;
						}
						else {
							percentRight = (int)(Math.abs(speed) + Math.max((x*100)/6, -100))*dir;
							percentLeft = (int)speed*dir;
						}
					}
				}
				tiltInfoDisplay.setText("Percent Left = " + percentLeft + ", Percent Right = " + percentRight);
				if(tiltcounter >= counterLimit){
					String str = "moveR" + percentRight + "L" + percentLeft;
					byte[] data = new byte[1024];
					DatagramPacket dPack = new DatagramPacket(data,1024);
					dPack.setData(str.getBytes());
					try {
						if(outSocket != null){
							outSocket.send(dPack);
							//System.out.println("Not Null");
						}
						else{
							//System.out.println("Null");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tiltcounter = 0;
				}
				else {
					tiltcounter++;
				}
				
							
			}//boolean tiltEnabled, within method onSensorChanged
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
			tvFacing.setText("Facing (Degrees): " + Double.toString(facing) );  //degrees symbol here
		}
	} //end method onSensorChanged

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
	
		
	//parses the location into a String in the desired format
	public static String parseLocation (double lat, double lon){
		return "lat" + lat + "lon" + lon;
		
	}
	
	//takes the string representation of location into destLat, destLon values
	//returns the latitude part 
	public static double reverseParseLat (String str) {
		int a= 3; //index of first char after "lat"
		int b = str.indexOf("lon"); //index of the "l" in "lon"
		return  Double.parseDouble(str.substring(a, b));
				
	}
	
	
	//returns the longitude part as a double
	public static double reverseParseLon (String str){
		int c= str.indexOf("lon") + 3; //index of the character after "lon"
		return Double.parseDouble(str.substring(c));
		
	}
	
	//
	/** onRemoteClick: Handles all GUI button clicks for directional remote controller
	 *  This method was modified because the remote control has been moved to a fragment.
	 * @param view (the button being clicked)
	 *
	 */
	public void onRemoteClick (View view) {
		String str ="";
		try {
			
			//get String from GUI:
			Button pressedButton = (Button) view;
			str = pressedButton.getText().toString();
		
				byte[] data = new byte[1024];
				DatagramPacket dPack = new DatagramPacket(data,1024);
				dPack.setData(str.getBytes());
				outSocket.send(dPack);   //sends message to VEX
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	///---------------------------------------------------------
		/// new methods for calling the Fragment GUI:
	
	/**
	 * displays the Remote control fragment
	 */
	public void showRemoteController(View v) {
	    DialogFragment newFragment = new RemoteControlFragment();
	    newFragment.show(getFragmentManager(), "Remocon");  //shows the speed changing dialog box
	}
	
	
	/**
	 * displays a new instance of the speedPicker
	 * Called from the GUI button click
	 * @param v
	 */
		public void showSpeedPickerDialog(View v) {
		    DialogFragment newFragment = new SpeedPickerFragment();
		    newFragment.show(getFragmentManager(), "speedPicker");  //shows the speed changing dialog box
		}

		//if the user clicks the 'ChangeSpeed' button on the dialog box
		/**
		 * Sends the changed speed variable to the VEX
		 */
		@Override
		public void onDialogPositiveClick(DialogFragment dialog) {
			SpeedPickerFragment myFragment = (SpeedPickerFragment) dialog;
			String str = myFragment.speed_str;  //get speed string from the Dialog box
			
			currentSpeed = myFragment.speed;  //update persistent currentSpeed variable.
			
			//send UDP:
					
	try {
					byte[] data = new byte[1024];
					DatagramPacket dPack = new DatagramPacket(data,1024);
					dPack.setData(str.getBytes());
					outSocket.send(dPack);  //send speed change command to VEX
				
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
		
		
		
		///--------------------------
	///// New methods for calling and responding to the WayPoint Select
		
		public void showWayPointPickerDialog(View v) {
		    DialogFragment newFragment = new LatLonPickerFragment();
		    newFragment.show(getFragmentManager(), "LatLonPicker");  //shows the speed changing dialog box; quotes is unique name
		}
		
		
		/**
		 * When user selects a new Way Point from the Picker Fragment,
		 * communicate to the phone to go to that waypoint.
		 * message is a latitude-longitude string in the format latxx.xxxxxlonxx.xxxxx where x is a digit
		 */
		 public void onWPSelectPositiveClick(DialogFragment dialog){
			 
			 LatLonPickerFragment myFragment = (LatLonPickerFragment) dialog;
				String str = myFragment.selectedWP;  //get selected waypoint string from the Dialog box
				
				//change the target destination coordinates to the selected waypoint
				destLat = reverseParseLat(str);  //updates the destination coordinates.
				destLon = reverseParseLon(str);
				//changes the target destination coordinates on the map:
				if (myMap !=null){
					destMarker.setPosition(new LatLng(destLat, destLon));
				}
				//send UDP:
				// message is a latitude-longitude string in the format latxx.xxxxxlonxx.xxxxx
				try {
								byte[] data = new byte[1024];
								DatagramPacket dPack = new DatagramPacket(data,1024);
								dPack.setData(str.getBytes());
								outSocket.send(dPack);  //send command to Phone
							
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
			 
		 }
	      public void onWPSelectNegativeClick(DialogFragment dialog){
	    	//do nothing on cancel
	    	      	  
	      }
	
	      
	   //////methods for dealing with Way Point Adder
	/**
	 * Displays a new Dialog box
	 * in response to the GUI button click for "Create New Way Point"
	 * @param v
	 */
			public void showWayPointAdderDialog(View v) {
			    DialogFragment newFragment = new WayPointAdderFragment();
			    newFragment.show(getFragmentManager(), "AddNewWP");  //shows dialog box; quotes is unique name
			}
			
			
		    /**Responds to positive click in the dialog fragment
		     * For creating a new WayPoint at the Current Location
		     * @param dialog
		     */
			@Override
			public void onWPAdderPositiveClick(DialogFragment dialog) {
				WayPointAdderFragment myFragment = (WayPointAdderFragment) dialog;
				String coords = myFragment.myLoc;  //gets the lat/lon string from the fragment
												   //however, this is almost unnecessary because the dialog fragment got that info from this class...
				//add the current coords to the waypoint list:
				waypointList.add(coords);
				//TODO: Error checking for duplicate coordinates, etc. 
			}

			@Override
			public void onWPAdderNegativeClick(DialogFragment dialog) {
				// do nothing on cancel
				
			}
			
						
			
	///////Methods for Adding WP manually.
			/**
			 * displays a new dialog for manual
			 * called by GUI
			 * @param v
			 */
			public void showWayPointManualAdderDialog(View v) {
			    DialogFragment newFragment = new ManualWayPointFragment();
			    newFragment.show(getFragmentManager(), "AddNewWPManually");  //shows the speed changing dialog box; quotes is unique name
			}
			
			
	      
			/**Responds to positive click in the dialog fragment
			 * for manually typing in latitude longitude values for a new way point.
			 * Adds the new location to the waypoint list.
			 */
	      @Override
	      public void onWPManualPositiveClick(DialogFragment dialog) {
	
	    	  double latv; //= myFragment.latv;
	    	  double lonv; //= myFragment.lonv;
	    	  
          	//get values from the EditText boxes
	    	  Dialog myDialog = dialog.getDialog();
          	EditText latText = (EditText) myDialog.findViewById(R.id.lat_value);   //findViewById is a method for the Activity class
          	String latStr = latText.getText().toString();
          	EditText lonText = (EditText) myDialog.findViewById(R.id.lon_value);
          	String lonStr = lonText.getText().toString();
          
          	
          	//check that the input values are numerical, and can be parsed without error.
          	try {
          		latv = Double.parseDouble(latStr);
          	   
          	}
          	catch (NumberFormatException e){
          		latv = 0; //set to something that won't crash the app as a temporary stop gap measure
          		e.printStackTrace();
          		
          	}
          	
          	try{
        	lonv = Double.parseDouble(lonStr);
          	}
          	catch (NumberFormatException e){
          		lonv = 0; //set to something that won't crash the app as a temporary stop gap measure
          		e.printStackTrace();
          	}
            
	    	  String coords = parseLocation(latv, lonv);  //get the doubles formatted correctly.
				//add the current coords to the waypoint list:
				waypointList.add(coords);
	    	  
	      }
	      
	      
	      @Override
		   public void onWPManualNegativeClick(DialogFragment dialog){
		        	//do nothing on cancel
		        	
		        }
			
			
	      /**
		     * Implements the interface onMapLongClickListener.
		     * When user "long presses" on the map do this.  
		     */
		    public void onMapLongClick (LatLng pnt){
		    	//update the mapLocation string to the value of the clicked point on the map.
		    	mapPoint = parseLocation (pnt.latitude, pnt.longitude);
		    	
		    	//show the fragment dialog box
			    DialogFragment newFragment = new MapWayPointAdderFragment();
			    newFragment.show(getFragmentManager(), "AddNewWPfromMap");  //shows the speed changing dialog box; quotes is unique name
			
		    	
		    	//TODO
		    	
		    	
		    } //end method onMapLongClick
		    
		
	      
	      
	      
	      
	      
	   //// Come Home Command
	      /**
	       * Responds to Button click in GUI
	       * parses Tablet's current location into a string
	       * and sends it to the phone.
	       */
	    public void comeHomeOnClick (View view) {
	    	
			//send UDP:
			// message is a latitude-longitude string in the format latxx.xxxxxlonxx.xxxxx
			try {
							byte[] data = new byte[1024];
							DatagramPacket dPack = new DatagramPacket(data,1024);
							dPack.setData(myLocation.getBytes());  //my location was updated in onLocationChanged
							outSocket.send(dPack);  //send to Phone
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
	    	
	    	
	    }//end method comeHome
	    
	    
	    
	    /**
	     * Tilt control
	     * @param view
	     */
	    
	    public void onToggleClicked(View view) {
	        // Is the toggle on?
	        boolean on = ((ToggleButton) view).isChecked();
	        
	        if (on) {
	            // Enable tilt control
	        	tiltEnabled =true;
	        } else {
	            // Disable tilt control
	        	tiltEnabled = false;
	        	tiltInfoDisplay.setText("Tilt Control is Off");
	        }
	    }
	    
	    
	    
	    
	    /**
	     * Sends the first waypoint in the list, and increments the waypoint counter variable.
	     * Assumes that there is at least one waypoint in the list, with index 0.
	     */
	    public void runProgramOnClick(View view) {
	    	waypointCounter = 0; //reset to 0.
	    	
			//get the next/current waypoint info from the waypoint list
			String str = waypointList.get(waypointCounter);
			
			//change the target destination coordinates to the selected waypoint
			destLat = reverseParseLat(str);  //updates the destination coordinates.
			destLon = reverseParseLon(str);
			//changes the target destination coordinates on the map:
			if (myMap !=null){
				destMarker.setPosition(new LatLng(destLat, destLon));
			}
			
			//send UDP:
			// message is a latitude-longitude string in the format latxx.xxxxxlonxx.xxxxx
			try {
							byte[] data = new byte[1024];
							DatagramPacket dPack = new DatagramPacket(data,1024);
							dPack.setData(str.getBytes());
							outSocket.send(dPack);  //send command to Phone
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
			
			waypointCounter++; //increment to next waypoint in the list.
	    	
	    } //end method runProgram onClick.
	    
	    


//////////////////////// Nested Classes //////////////////////
	
	/**CommunicationThread: Communicates with other Android
	 * Does the actual connection and sending.
	 * 
	 * @author Jacob
	 *
	 */
	class CommunicationThread implements Runnable {

		private BufferedReader input;
		
		private DatagramSocket receiveSocket;
		
		public CommunicationThread() {
			try {
				receiveSocket = new DatagramSocket(TABLETPORT);  //listening port
				//text.setText(text.getText().toString()+"My IP Address is: "+ getIpAddr() + "\n" + "My Port Number: " + TABLETPORT + "\n");
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
	}// end class CommunicationThread
	
	/**ClientThread: Decides who to connect to (usually the phone)
	 * 
	 * @author Jacob
	 *
	 */
	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				outSocket = new DatagramSocket();
				//outSocket.connect(InetAddress.getByName(VEX_IP), VEXPORT);  //outSocket connects to VEX
				outSocket.connect(InetAddress.getByName(PHONE_IP), PHONEPORT);  //outSocket connects to Phone

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	} //end class ClientThread

	/**ProcessMessageThread: 
	 * receives messages from phone regarding phone's position
	 * and displays processes them to display on the map.
	 * @author Jacob; 
	 *
	 */
	class ProcessMessageThread implements Runnable {
		private String msg;

		public ProcessMessageThread(String str) {
			this.msg = str;
		}

		//receive
		@Override
		public void run() {
			
			if  (text.getText().length() > 25000){	
				//this is to prevent the app from being bogged down by endless messages.
				text.setText("Older Msgs have been truncated.\n"); 
			}
			
			text.setText(text.getText().toString()+"Client Says: "+ msg + "\n"); //displays what the phone said on Tablet screen
			
			//move the phone's marker on the map
			if(msg.startsWith("lat") && (myMap !=null)) {
				phoneMarker.setPosition(new LatLng(reverseParseLat(msg), reverseParseLon(msg)));
			}
			
			//if the message is the phone reporting back that it reached a Waypoint, give the next waypoint on the list to the Phone.
			if (msg.startsWith("reachedWP")){
				//TODO
				//error check to make sure that  the count is within the bounds of the waypointList
				if (waypointCounter >= waypointList.size()){
					//print that we are at the last waypoint, and don't send the next waypoint
					text.append("We have reached the last waypoint in the list.\n");
				}
				else {
				//get the next/current waypoint info from the waypoint list
				String str = waypointList.get(waypointCounter);
				
				//change the target destination coordinates to the selected waypoint
				destLat = reverseParseLat(str);  //updates the destination coordinates.
				destLon = reverseParseLon(str);
				//changes the target destination coordinates on the map:
				if (myMap !=null){
					destMarker.setPosition(new LatLng(destLat, destLon));
				}
				
				//send UDP:
				// message is a latitude-longitude string in the format latxx.xxxxxlonxx.xxxxx
				try {
								byte[] data = new byte[1024];
								DatagramPacket dPack = new DatagramPacket(data,1024);
								dPack.setData(str.getBytes());
								outSocket.send(dPack);  //send command to Phone
							
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
				
				waypointCounter++; //increment to next waypoint in the list.
				}
			}
			
			
			//Following deprecated code assumes that the current device is a phone
			//and relays messages to VEX
			/*
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
					outSocket.send(dPack);  //sends to VEX
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
		} //end method run
	} //end class ProcessMessageThread 


	

}