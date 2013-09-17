package bigarms.COEN4720.ssh_test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SshNetwork implements Runnable {

	private JSch jsch;
	private Session session;
	private PipedInputStream iSendStream;
	private PipedOutputStream oSendStream;
	private PipedInputStream iRcvStream;
	private PipedOutputStream oRcvStream;
	private SshActivity activity;
	private PipeReader pr;
	private Thread prThread;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		iSendStream = new PipedInputStream();
		oSendStream = new PipedOutputStream();
		
		iRcvStream = new PipedInputStream();
		oRcvStream = new PipedOutputStream();
		
		try {
			oSendStream.connect(iSendStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			oRcvStream.connect(iRcvStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		jsch = new JSch();
		try {
			//get a session with the remote server
			session = jsch.getSession(activity.getString(R.string.username_morbius), activity.getString(R.string.hostname_morbius));
			session.setPassword(activity.getString(R.string.password_morbius));
			
			UserInfo ui = new MyUserInfo();
			((MyUserInfo) ui).setActivity(activity);
			session.setUserInfo(ui);
			session.connect(30000);
			
			//open a command channel
			//Channel channel = session.openChannel("shell");
			//channel.setInputStream(iSendStream);
			//channel.setOutputStream(oRcvStream);
			//((TextView) activity.findViewById(R.id.connectionText)).setText("Connected!");
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PipeReader pr = new PipeReader(iRcvStream,activity);
		prThread = new Thread(pr);
	}
	
	public void setActivity(SshActivity act){
		activity = act;
	}
	
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
