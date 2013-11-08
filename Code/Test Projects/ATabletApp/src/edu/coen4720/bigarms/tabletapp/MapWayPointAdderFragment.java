package edu.coen4720.bigarms.tabletapp;

import android.app.Activity;

/**
 * Fragment that is created in response to long pressing on a point on the map  in the GUI.
 * Called from the main activity.
 * The code and functionality is almost exactly identical to the "WayPointAdderFragment" class,
 * except that this one will add a WP to the point clicked on the map, and not to the current location of the tablet.
 * @author SJ
 *
 */

public class MapWayPointAdderFragment extends WayPointAdderFragment{


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
          myLoc  = ((TabletMainActivity) activity ).mapPoint;  //this line is the only difference with WayPointAdderFragment
          													  // Everything else will work with existing code, because 
          													// from this point forward, everything depends on the myLoc value.
     } //end override onAttach
	
}
