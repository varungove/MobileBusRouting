package uiuc.mbr.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import uiuc.mbr.calendar.Event;
import uiuc.mbr.event_selection.RecurringEventList;

/**Dialog triggered when the user tries to add a recurring event to the schedule.
 * Asks the user whether they want to add all instances or just one.*/
public class AddRecurringEventDialog extends DialogFragment
{
	public static void setup(Event event, Bundle args)
	{
		event.export("", args);
	}
	private void readArgs()
	{
		this.event = Event.importFrom("", getArguments());
	}

	private Event event;


	@Override
	public Dialog onCreateDialog(Bundle saved)
	{
		readArgs();

		return new AlertDialog.Builder(getActivity())
		.setMessage("This event is recurring.  Would you like all future instances of this event to automatically be added to the schedule?")

		// Set up the buttons
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RecurringEventList.add(event, getActivity().getApplicationContext());
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		})
		.create();
	}

}
