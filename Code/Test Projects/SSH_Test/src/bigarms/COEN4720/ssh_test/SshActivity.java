package bigarms.COEN4720.ssh_test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.jcraft.jsch.*;

public class SshActivity extends FragmentActivity implements
		ActionBar.TabListener, YesNoDialogFragment.YesNoDialogListener{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private boolean tempBool;
	private String retText;
	public final Semaphore dialogWait = new Semaphore(1);
	private Thread prThread;
	private SshNetwork network;
	private Thread netThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		tempBool = false;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ssh);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		network = new SshNetwork();
		network.setActivity(this);
		netThread = new Thread(network);
		
//		PipedInputStream iSendStream = new PipedInputStream();
//		PipedOutputStream oSendStream = new PipedOutputStream();
//		try {
//			oSendStream.connect(iSendStream);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		PipedInputStream iRcvStream = new PipedInputStream();
//		PipedOutputStream oRcvStream = new PipedOutputStream();
//		try {
//			oRcvStream.connect(iRcvStream);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		jsch = new JSch();
//		try {
//			//get a session with the remote server
//			session = jsch.getSession(getString(R.string.username_morbius), getString(R.string.hostname_morbius));
//			session.setPassword(getString(R.string.password_morbius));
//			
//			UserInfo ui = new MyUserInfo();
//			((MyUserInfo) ui).setActivity(this);
//			session.setUserInfo(ui);
//			//session.connect(30000);
//			
//			//open a command channel
//			//Channel channel = session.openChannel("shell");
//			//channel.setInputStream(iSendStream);
//			//channel.setOutputStream(oRcvStream);
//		} catch (JSchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		PipeReader pr = new PipeReader(iRcvStream,this);
//		prThread = new Thread(pr);
		/*Button button = (Button) findViewById(R.id.buttonGo);
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        prThread.run();
		    }
		});*/ //button is null
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ssh, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	public void showYesNoDialog(String message){
		try {
			dialogWait.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DialogFragment dialog = new YesNoDialogFragment();
		dialog.show(getFragmentManager(), "NoticeDialogFragment");
	}
	
	public void onDialogPositiveClick(DialogFragment dialog){
		//user touched ok
		tempBool = true;
		dialogWait.release();
	}
	
	public void onDialogNegativeClick(DialogFragment dialog){
		//user touched cancel
		tempBool = false;
		dialogWait.release();
	}
	
	//connect button click
	public void onConnectClick(View view){
		((TextView) findViewById(R.id.connectionText)).setText("Connect button pushed");
		//Thread netThread = new Thread(network);
		netThread.run();
	}
	
	public boolean getTempBool(){
		return tempBool;
	}
	
	public String getRetText(){
		return retText;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			// This is only called once when the app is started.
			
			Bundle args;
			Fragment fragment;
			switch (position) {
			case 0:
				fragment = new ConnectionFragment();
				args = new Bundle();
				args.putInt(ConnectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
				
			case 1:
				fragment = new CommandFragment();
				args = new Bundle();
				args.putInt(CommandFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
				
			default:
				fragment = new DummySectionFragment();
				args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
				
			}
			/*starter code
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
			*/
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_connection).toUpperCase(l);
			case 1:
				return getString(R.string.title_command).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_ssh_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
	/**
	 * A first attempt at creating another fragment separate from the dummies
	 * that still displays dummy text.
	 */
	public static class ConnectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public ConnectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_connect,
					container, false);
			TextView connectionTextView = (TextView) rootView
					.findViewById(R.id.connectionText);
			connectionTextView.setText("Connection View\nPage: " + Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	/**
	 * A second attempt at creating another fragment separate from the dummies
	 * that still displays dummy text.
	 */
	public static class CommandFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public CommandFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_command,
					container, false);
			TextView connectionTextView = (TextView) rootView
					.findViewById(R.id.commandText);
			connectionTextView.setText("Command View\nPage: " + Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
	/**
	 * UserInfo class implementation for sch
	 */
	public static class MyUserInfo implements UserInfo{
		String passwd;
		SshActivity activity;
		
		public String getPassword(){ return passwd; }
		public boolean promptYesNo(String str){
			boolean retVal;
			
			//instantiate dialog from activity
			activity.showYesNoDialog(str);
			//attempt to acquire semaphore to wait for tempBool to be set by dialog.
			try {
				activity.dialogWait.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			retVal = activity.getTempBool();
			activity.dialogWait.release();
			
			return retVal;
		}
		
		public String getPassphrase(){ return null; }
		public boolean promptPassphrase(String message){ return true; }
		public boolean promptPassword(String message){
			boolean retVal;

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Password Prompt");
			
			//create text edit input
			final EditText input = new EditText(activity);
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			builder.setView(input);
			
			// Set up the buttons
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        passwd = input.getText().toString();
			    }
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        passwd = null;
			    	dialog.cancel();
			    }
			});
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
			retVal = (passwd != null);
			
			return retVal;
		}
		
		public void showMessage(String message){
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("showMessage")
				.setMessage(message);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		public void setActivity(SshActivity act){
			activity = act;
		}
	}
}
