package edu.coen4720.bigarms.tabletapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;


/**
 * Dialog Pop-up Fragment that allows user to manually enter 
 * Latitude and Longitude values for a new Way Point that will be added to the 
 * WayPoint list in the MainActivity. 
 * @author SJ
 *
 * Partial list of references: 
 * http://developer.android.com/guide/topics/ui/dialogs.html
 * http://developer.android.com/reference/android/view/LayoutInflater.html
 */


public class ManualWayPointFragment extends DialogFragment{
	
	/////my own listener interface:
	public interface WPManualListener {
        public void onWPManualPositiveClick(DialogFragment dialog);
        public void onWPManualNegativeClick(DialogFragment dialog);

	}   //end interface WPManualListener
	
	WPManualListener myListener;  //listener object to pass back results
	double latv;
	double lonv;  
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            myListener = (WPManualListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        
        
     } //end override onAttach
	
	
	/**
	 * Creates a floating Dialog Box with the WayPoint picker and 2 buttons
	 */
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		 // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set the dialog box's message:
		builder.setMessage(" Please enter the numerical Latitude and Longitude\nvalues for the new Way Point.\n" );
							
		// Add the text boxes for lat lon
		 // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();  //instantiates a xml file into a View object

		builder.setView(inflater.inflate(R.layout.frag_manual_latlon, null));  //uses the .xml layout as the contents of this alert box
		
		//Add OK and Cancel buttons to the dialog:
		builder.setPositiveButton(R.string.dialog_add_waypoint, new DialogInterface.OnClickListener()   
		{
            public void onClick(DialogInterface dialog, int id) {
            	
       	
            	// pass back changes
            	myListener.onWPManualPositiveClick(ManualWayPointFragment.this); //pass back
            	
            	
            }
        });
		builder.setNegativeButton(R.string.dialog_cancel_change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            	//do nothing.
            	myListener.onWPManualNegativeClick(ManualWayPointFragment.this); //pass back
            }
        });
					
		//returns the alertDialog created by the builder:
		return builder.create();

	} //end method onCreate
    
    
    
    
	
} //end class ManualWPFragment
