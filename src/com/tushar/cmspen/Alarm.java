package com.tushar.cmspen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Alarm extends BroadcastReceiver {
	public static final int ONE_SECOND = 1000;
	public static final int ONE_MINUTE = ONE_SECOND * 60;
	public static final int REFRESH_TIME = ONE_MINUTE * 30;
	static AlarmManager am;
	static PendingIntent pi;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(pref.getBoolean("enabled", false))
		{
			MainActivity.StopEventMonitor(ctx);
			MainActivity.StartEventMonitor(ctx);
			startAlarm(ctx);
		}
	}
	
	static void startAlarm(Context ctx)
	{
		am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		pi = PendingIntent.getBroadcast(ctx, 0, new Intent("com.tushar.cmspen.REFRESH"), 0);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + REFRESH_TIME, pi);
	}
	
	static void cancelAlarm()
	{
		am.cancel(pi);
	}
}
