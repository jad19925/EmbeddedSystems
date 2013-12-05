package edu.coen4720.bigarms.tabletapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;


/** A fragment that shows the remote controller from project iteration 1.
 * Saves space on the main layout of the app.
 * @author SJ
 * Adapted from the other fragment classes already created for this project.
 */

public class RemoteControlFragment extends DialogFragment{
		
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        
     } //end override onAttach
	
	
	
	
	
	/**
	 * Creates a floating Dialog Box with the remote control layout.
	 */
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		 // Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set the dialog box's message:
		//builder.setMessage("Remote Controller\n" );
							
		// Add the text boxes for lat lon
		 // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();  //instantiates a xml file into a View object

		builder.setView(inflater.inflate(R.layout.controller, null));  //uses the .xml layout as the contents of this alert box
		
		//Add OK  button to the dialog:
		builder.setPositiveButton("Close", null);
		
					
		//returns the alertDialog created by the builder:
		return builder.create();

	} //end method onCreate
	
	

	
	
}
