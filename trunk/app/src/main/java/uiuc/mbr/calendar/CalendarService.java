package uiuc.mbr.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * API for accessing the Android calendar and events.
 */
public class CalendarService {
	private static final String[] CALENDAR_PROJECTION = new String[]{
			CalendarContract.Calendars._ID,
			CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
	};

	// The indices for the projection array above.
	private static final int CALENDAR_ID_INDEX = 0;
	private static final int CALENDAR_DISPLAY_NAME_INDEX = 1;

	//Instances are occurrences of recurring events
	private static final String[] INSTANCE_PROJECTION = new String[]{
			CalendarContract.Instances.CALENDAR_ID,
			CalendarContract.Instances.EVENT_ID,
			CalendarContract.Instances.TITLE,
			CalendarContract.Instances.DESCRIPTION,
			CalendarContract.Instances.EVENT_LOCATION,
			CalendarContract.Instances.BEGIN,
			CalendarContract.Instances.END
	};

	private static final int EVENT_CALENDAR_ID_INDEX = 0;
	private static final int EVENT_PARENT_ID_INDEX = 1;
	private static final int EVENT_TITLE_INDEX = 2;
	private static final int EVENT_DESCRIPTION_INDEX = 3;
	private static final int EVENT_LOCATION_INDEX = 4;
	private static final int EVENT_BEGIN_INDEX = 5;
	private static final int EVENT_END_INDEX = 6;

	private static final String[] EVENT_PROJECTION = new String[] {
			CalendarContract.Events.RRULE
	};

	private static final long MILLISECONDS_IN_DAY = 86400000;

	private static final Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/calendars");
	private static final Uri INSTANCE_URI = Uri.parse("content://com.android.calendar/instances/when");

	private Context context;

	public CalendarService(Context context) {
		this.context = context;
	}

	/**
	 * Gets all calendars associated the current Google account on the device.
	 * @return An ArrayList of Calendars, null if no account was found.
	 */
	@Nullable
	public ArrayList<Calendar> getCalendars() {
		// Run query
		ContentResolver cr = context.getContentResolver();
		String selection = "((" + CalendarContract.Calendars.VISIBLE + " = ?) AND (" +
				CalendarContract.Calendars.SYNC_EVENTS + " = ?))";

		String[] selectionArgs = new String[]{"1", "1"};
		ArrayList<Calendar> cals = new ArrayList<>();
		// Submit the query and get a Cursor object back.
		try(Cursor cur = cr.query(CALENDAR_URI, CALENDAR_PROJECTION, selection, selectionArgs, null)) {
			if(cur != null) {
				while(cur.moveToNext()) {
					// Get the field values
					long calID = cur.getLong(CALENDAR_ID_INDEX);
					String displayName = cur.getString(CALENDAR_DISPLAY_NAME_INDEX);

					cals.add(new Calendar(calID, displayName));
				}
			}
		}

		return cals;
	}

	/**
	 * @return All events in the next 24 hours.
	 */
	public ArrayList<Event> getEventsNext24Hours() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		long now = c.getTimeInMillis();
		return getEvents(now, now+MILLISECONDS_IN_DAY);
	}

	/**
	 * Finds all events that start between startTime and endTime.
	 * @param startTime Start time in UTC millis.
	 * @param endTime End time in UTC millis.
	 * @return An ArrayList of all Events starting between startTime and endTime.
	 */
	public ArrayList<Event> getEvents(long startTime, long endTime) {
		ContentResolver cr = context.getContentResolver();

		String selection = "(( " + CalendarContract.Instances.ALL_DAY + " = ?))";
		String[] selectionArgs = new String[]{"0"};
		Uri fullUri = Uri.parse(INSTANCE_URI+"/"+startTime+"/"+endTime);
		Cursor cur = cr.query(fullUri,
				INSTANCE_PROJECTION,
				selection,
				selectionArgs,
				null);

		ArrayList<Event> eventList = new ArrayList<>();
		while (cur.moveToNext()) {
			long calID;
			long parentEventId;
			String title;
			String description;
			String location;
			long begin;
			long end;

			// Get the field values
			calID = cur.getLong(EVENT_CALENDAR_ID_INDEX);
			parentEventId = cur.getLong(EVENT_PARENT_ID_INDEX);
			title = cur.getString(EVENT_TITLE_INDEX);
			description = cur.getString(EVENT_DESCRIPTION_INDEX);
			location = cur.getString(EVENT_LOCATION_INDEX);
			begin = cur.getLong(EVENT_BEGIN_INDEX);
			end = cur.getLong(EVENT_END_INDEX);

			eventList.add(new Event(calID, parentEventId, title, description, location, new Date(begin), new Date(end)));
		}

		cur.close();

		return eventList;
	}

	/**
	 * Checks if the given event's parent is recurring
	 */
	public boolean isEventRecurring(Event event) {
		long eventId = event.getParentEventId();

		// Run query
		Cursor cur;
		ContentResolver cr = context.getContentResolver();

		Uri u = ContentUris.withAppendedId(Uri.parse("content://com.android.calendar/events"), eventId);

		boolean isRecurring = false;

		// Submit the query and get a Cursor object back.
		try {
			cur = cr.query(u, EVENT_PROJECTION, null, null, null);

			while (cur.moveToNext()) {
				String rrule;

				// Get the field values
				rrule = cur.getString(0);

				Log.d("IsEventRecurring", "RRule = " + rrule);

				if (rrule != null)
					isRecurring = true;
			}

			cur.close();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return isRecurring;
	}
}
