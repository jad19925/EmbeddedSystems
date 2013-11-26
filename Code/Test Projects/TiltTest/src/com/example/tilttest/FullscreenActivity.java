package com.example.tilttest;

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
	private TextView textView = null;
	private float[] gravity = new float[3];

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

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		textView = (TextView)findViewById(R.id.textView1);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
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

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
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
			
			textView.setText("x: " + gravity[0] + ", y: " + gravity[1] + ", z: " + gravity[2]);
			
			//calibrate for flatness on a moving earth
			x -= 0.065;
			y -= 0.133;
			
			//tilted all the way to the right
			if(Math.abs(x/y) > 6 && x > 0)
			{
				percentRight = 100;
				percentLeft = -100;
			}
			//tilted all the way left
			else if(Math.abs(x/y) > 6 && x < 0)
			{
				percentRight = -100;
				percentLeft = 100;
			}
			//moving zero
			else if(x < 1.5 && y < 1.5)
			{
				percentRight = 0;
				percentLeft = 0;
			}
			else
			{
				percentRight = (int)maxAbs((x * 100 / 6), 100);
				percentLeft = (int)maxAbs((-x * 100 / 6), 100);
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
}