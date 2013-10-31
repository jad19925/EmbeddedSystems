package edu.coen4720.bigarms.tabletapp;

/**
 * This class contains code to create and listen to a GUI Fragment that can
 * Select a WayPoint, from a list of WayPoints.
 * Author: SJK
 */


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

public class LatLonPickerFragment extends DialogFragment{
	
	NumberPicker WayPointPicker;  //The picker itself
	ArrayList <String> wplist;
	String[] wpArray;
	String selectedWP;  //way point that will be passed back to the main Activity
	
	/////my own listener interface:
	public interface WPSelectListener {
        public void onWPSelectPositiveClick(DialogFragment dialog);
        public void onWPSelectNegativeClick(DialogFragment dialog);

	}   //end interface WPSelectListener
	
	
    // Use this instance of the interface to deliver action events
	WPSelectListener  myListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            myListener = (WPSelectListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        //update list of Way Points from the main class to match the client every time the dialog is created:
          wplist  = ((TabletMainActivity) activity ).waypointList;

    } //end override onAttach
	
	/**
	 * Creates a floating Dialog Box with the WayPoint picker and 2 buttons
	 */
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		 // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set the dialog box's message:
		builder.setMessage(R.string.dialog_wp_pick_message);
							
		// create a picker widget for the speed:
		WayPointPicker = new NumberPicker(getActivity());
		WayPointPicker.setMinValue(0);
		WayPointPicker.setMaxValue(wplist.size() -1);
		wpArray = new String[wplist.size()];
		wpArray =  wplist.toArray(wpArray);
	
		WayPointPicker.setDisplayedValues(wpArray);  //cast to String array to display in Picker
		WayPointPicker.setWrapSelectorWheel(false);
		
		//add the picker to the dialog:
		builder.setView(WayPointPicker);
		
		
		//Add OK and Cancel buttons to the dialog:
		builder.setPositiveButton(R.string.dialog_select_waypoint, new DialogInterface.OnClickListener()   
		{
            public void onClick(DialogInterface dialog, int id) {
                // change speed here. 
            	//set the new Speed to the current speed value: (newSpeed should have been changed by the listener)
            	
            	selectedWP = wpArray[WayPointPicker.getValue()];  //use the index returned by getValue to select correct string in wpArray and wpList

            	
            	// pass back changes
            	myListener.onWPSelectPositiveClick(LatLonPickerFragment.this); //pass back
            	
            	
            }
        });
		builder.setNegativeButton(R.string.dialog_cancel_change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            	//do nothing.
            	myListener.onWPSelectNegativeClick(LatLonPickerFragment.this); //pass back
            }
        });
					
		//returns the alertDialog created by the builder:
		return builder.create();

	} //end method onCreate
	
	
	
	

}
