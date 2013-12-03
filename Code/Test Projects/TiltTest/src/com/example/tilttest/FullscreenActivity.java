package com.example.tilttest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.tilttest.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private static final boolean AUTO_HIDE = true;
	private float[] accValues = null;
	private float[] magValues = null;
	private TextView textView1 = null;
	private TextView textView2 = null;
	private float[] gravity = new float[3];
	private int counter = 0;
	private final int counterLimit = 10;
	
	//wifi code declarations
	private DatagramSocket dSocket;
	private static final int SERVERPORT = 9923;//8080;//Open Port on Android Devices
	private static final String SERVER_IP = "192.168.1.127";

	@Override
	protected void onPause()
	{
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_UI);
	}
	
	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		
		final View contentView = findViewById(R.id.fullscreen_content);
		new Thread(new ClientThread()).start();

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		textView1 = (TextView)findViewById(R.id.textView1);
		textView2 = (TextView)findViewById(R.id.textView2);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		float[] R = new float[9];
		float[] I = new float[9];
		float[] orientation = new float[3];
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			float x, y, z;
			int percentLeft = 0, percentRight = 0;
			
			accValues = event.values;
			final float alpha = 0.8f;
			x = gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			y = gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			z = gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			
			textView1.setText("x: " + gravity[0] + ", y: " + gravity[1] + ", z: " + gravity[2]);
			
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
			textView2.setText("Percent Left = " + percentLeft + ", Percent Right = " + percentRight);
			if(counter >= counterLimit){
				String str = "moveR" + percentRight + "L" + percentLeft;
				byte[] data = new byte[1024];
				DatagramPacket dPack = new DatagramPacket(data,1024);
				dPack.setData(str.getBytes());
				try {
					if(dSocket != null){
						dSocket.send(dPack);
						//System.out.println("Not Null");
					}
					else{
						//System.out.println("Null");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				counter = 0;
			}
			else {
				counter++;
			}
		}
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			magValues = event.values;
		}
		
		if(magValues != null && accValues != null)
		{
			SensorManager.getRotationMatrix(R, I, accValues, magValues);
			SensorManager.getOrientation(R, orientation);
		}
	}
	private float maxAbs(float f1, float f2)
	{
		if(f1 > 0 && f2 > 0)
		{
			return Math.max(f1, f2);
		}
		else if (f1 > 0 && f2 < 0) 
		{
			if(f1 == Math.max(f1, -f2))
			{
				return f1; 
			}
			else
			{
				return f2;
			}
		}
		else if (f1 < 0 && f2 > 0)
		{
			if(f2 == Math.max(-f1, f2))
			{
				return f2; 
			}
			else
			{
				return f1;
			}
		}
		else
		{
			return Math.min(f1, f2);
		}
	}
	
	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				dSocket = new DatagramSocket();
				dSocket.connect(InetAddress.getByName(SERVER_IP), SERVERPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}
}
