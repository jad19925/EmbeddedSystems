package com.javacodegeeks.android.androidsocketclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

/// New GUI stuff for Deciding Speed variable:
public class SpeedPickerFragment extends DialogFragment 
 {
	NumberPicker speedpicker; 
	int speed = 8; // placeholder - actual default value is decided in the Client Class's currentSpeed variable
	String speed_str = "speed08"; //placeholder for default value
	
	
	//-----------------------------------
	
	//my own listener interface:
	public interface mySpeedChangeListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);

	}   //end interface mySpeedChangeListener
	
	
    // Use this instance of the interface to deliver action events
	mySpeedChangeListener  myListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            myListener = (mySpeedChangeListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        //update speed and speedString to match the client every time the dialog is created:
        speed = ((Client) activity ).currentSpeed;
        changeSpeedString();
    } //end override onAttach
	
	
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		 // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set the dialog box's message:
		builder.setMessage(R.string.dialog_speed_pick_message);
							
		// create a picker widget for the speed:
		speedpicker = new NumberPicker(getActivity());
		speedpicker.setMinValue(0);
		speedpicker.setMaxValue(32);
		speedpicker.setValue(speed); // default value
		speedpicker.setWrapSelectorWheel(false);
		
		//add the picker to the dialog:
		builder.setView(speedpicker);
		
		
		//Add OK and Cancel buttons to the dialog:
		builder.setPositiveButton(R.string.dialog_change_speed, new DialogInterface.OnClickListener()   
		{
            public void onClick(DialogInterface dialog, int id) {
                // change speed here. 
            	//set the new Speed to the current speed value: (newSpeed should have been changed by the listener)
            	
            	speed = speedpicker.getValue();
            	//create a speed string in the format "speedXX", where XX is a two digit number; then, send that string
            	changeSpeedString(); 
            	
            	// passing back the changed speed properly
            	myListener.onDialogPositiveClick(SpeedPickerFragment.this); //pass back
            	
            	
            }
        });
		builder.setNegativeButton(R.string.dialog_cancel_change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            	//do nothing.
            	myListener.onDialogNegativeClick(SpeedPickerFragment.this); //pass back
            }
        });
					
		//returns the alertDialog created by the builder:
		return builder.create();

	} //end method onCreate
	
	
	//create a speed string in the format "speedXX", where XX is a two digit number; then, send that string
	public void changeSpeedString() {
					
			//create String 
			String speednumber;
			if (speed <10) {
				speednumber = "0" + speed + "";
			}
			else {
				speednumber = speed + "";  //convert to String the silly way
			}
			speed_str = "speed" + speednumber;  //speedXX
					
	}
	
	

} //end class SpeedPicker Fragment