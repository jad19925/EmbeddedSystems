package bigarms.COEN4720.ssh_test;

import java.io.IOException;
import java.io.PipedInputStream;

import android.widget.TextView;
//import android.app.Activity;

public class PipeReader implements Runnable {

	private SshActivity activity;
	private PipedInputStream iStream;
	public PipeReader(PipedInputStream pis, SshActivity act){
		activity = act;
		iStream = pis;
	}
	
	@Override
	public void run() {
		TextView view = (TextView) activity.findViewById(R.id.outView);
		byte c[] = new byte[1];
		int i = 0;
		while(true){
			try {
				i = iStream.read(c);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(-1 != i){
				view.append(String.valueOf((char)c[0]));
			}
			else{
				view.append("\n");
			}
		}
	}

}
