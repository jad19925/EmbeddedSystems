package edu.coen4720.bigarms.tabletapp;

/**
 * This class contains code to create and listen to a GUI Fragment that can
 * Add a new WayPoint at the current location of the Tablet, to a list of WayPoints 
 * that are kept in the main activity class.
 * Author: SJK
 */



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class WayPointAdderFragment extends DialogFragment{

	
	/////my own listener interface:
	public interface WPAdderListener {
        public void onWPAdderPositiveClick(DialogFragment dialog);
        public void onWPAdderNegativeClick(DialogFragment dialog);

	}   //end interface WPAdderListener
	
	protected String myLoc; //my Location string, in the format latxx.xxxxlonxx.xxxx
	protected WPAdderListener myListener;  //listener object to pass back results
	
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            myListener = (WPAdderListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        //update myLocation from the main class to match the client every time the dialog is created:
          myLoc  = ((TabletMainActivity) activity ).myLocation;
     } //end override onAttach
	
	
	/**
	 * Creates a floating Dialog Box with the WayPoint picker and 2 buttons
	 */
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		 // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set the dialog box's message:
		builder.setMessage("Would you like to add a new waypoint here?\n Current Location: \n" + myLoc);
							
		
		
		//Add OK and Cancel buttons to the dialog:
		builder.setPositiveButton(R.string.dialog_add_waypoint, new DialogInterface.OnClickListener()   
		{
            public void onClick(DialogInterface dialog, int id) {
            	
            	// pass back changes
            	myListener.onWPAdderPositiveClick(WayPointAdderFragment.this); //pass back
            	
            	
            }
        });
		builder.setNegativeButton(R.string.dialog_cancel_change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            	//do nothing.
            	myListener.onWPAdderNegativeClick(WayPointAdderFragment.this); //pass back
            }
        });
					
		//returns the alertDialog created by the builder:
		return builder.create();

	} //end method onCreate
    
	
	
	
} //end class WayPointAdderFragment

