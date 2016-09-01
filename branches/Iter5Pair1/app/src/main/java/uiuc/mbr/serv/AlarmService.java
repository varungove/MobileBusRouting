package uiuc.mbr.serv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.*;

import uiuc.mbr.Alarm;
import uiuc.mbr.OnAlarmActivity;

/**Keeps track of alarms and when they should trigger.
 * Launches OnAlarmActivity when an alarm is triggered.
 * Don't launch this service directly--stick to using the static methods.*/
public class AlarmService extends Service
{
	private static final Queue<Alarm> untriggeredAlarms = new PriorityQueue<>();
	@Nullable private static Alarm triggeredAlarm = null;
	private static final Map<Long, Alarm> idsMap = new HashMap<>();


	public static void addAlarm(Alarm alarm, Context context)
	{
		if(idsMap.containsKey(alarm.event.getParentEventId()))
			return;
		untriggeredAlarms.add(alarm);
		idsMap.put(alarm.event.getParentEventId(), alarm);
		run(context);
	}

	public static void remove(long eventId, Context context)
	{
		Alarm alarm = idsMap.get(eventId);
		untriggeredAlarms.remove(alarm);
		if(triggeredAlarm == alarm)
			triggeredAlarm = null;
		idsMap.remove(eventId);
	}


	public static List<Alarm> getUntriggeredAlarms(){return new ArrayList<>(untriggeredAlarms);}

	@Nullable public static Alarm getTriggeredAlarm(){return triggeredAlarm;}
	public static void clearTriggeredAlarm(Context context)
	{
		if(triggeredAlarm == null)
			throw new IllegalStateException();
		triggeredAlarm = null;
		run(context);
	}

	public static void removeAll()
	{
		untriggeredAlarms.clear();
		triggeredAlarm = null;
		idsMap.clear();
	}

	/**Returns an alarm if we have one for that event.*/
	@Nullable public static Alarm getForEvent(long eventId)
	{
		Log.d("AlarmService", "get alarm when there are " + idsMap.size());
		return idsMap.get(eventId);
	}


	private static void run(Context context)
	{
		context.startService(new Intent(context, AlarmService.class));
	}



	@Nullable @Override
	public IBinder onBind(Intent intent){return null;}


	@Override
	public int onStartCommand(Intent i, int flags, int startId)
	{
		Alarm next = untriggeredAlarms.peek();
		if(next != null)
		{
			long nextTime = next.event.getStart().getTime();
			long now = System.currentTimeMillis();
			if(nextTime <= now)
			{
				Intent start = new Intent(getApplicationContext(), OnAlarmActivity.class);
				start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(start);
				untriggeredAlarms.poll();//remove
				if(triggeredAlarm != null)
					throw new IllegalStateException();
				triggeredAlarm = next;
			}
			else
			{
				AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				Intent innerAction = new Intent(this, AlarmService.class);
				PendingIntent action = PendingIntent.getService(this, 0, innerAction, 0);
				mgr.setExact(AlarmManager.RTC_WAKEUP, nextTime, action);
			}
		}

		return super.onStartCommand(i, flags, startId);
	}
}
