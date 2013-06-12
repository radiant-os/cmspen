/***
   Copyright 2013-2015 Tushar Dudani

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.tushar.cmspen;

import java.util.Timer;
import java.util.TimerTask;

import net.pocketmagic.android.eventinjector.Events;
import net.pocketmagic.android.eventinjector.Events.InputDevice;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class SPenDetection extends Service {
	Events events = new Events();
	static Vibrator v;
	int id = -1;
	public static int polling = 1000;
	public static int DET_POLLING = 20;
	BroadcastReceiver mReceiver;
	public static int pvalues[] = {100,250,500,750,1000,1250,1500,1750,2000,2250,2500,2750,3000,3250,3500,3750,4000};
	static Intent i = new Intent("com.samsung.pen.INSERT");
	public static Timer timer;
	static WakeLock screenLock;
	static InputDevice idev;
	static SharedPreferences pref;
	static SharedPreferences.Editor editor;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate()
	{
		events.Init();
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = pref.edit();
		polling = pvalues[pref.getInt("polling", 0)];
		id = pref.getInt("id", -1);
        if(id == -1)
        {
        	stopSelf();
        }
        v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        if(pref.getBoolean("soffchk", false))
        {
        	IntentFilter filter = new IntentFilter();
        	filter.addAction(Intent.ACTION_SCREEN_OFF);
        	filter.addAction(Intent.ACTION_SCREEN_ON);
        	mReceiver = new ScreenReceiver();
        	registerReceiver(mReceiver, filter);
        }
        try
        {
        	idev = events.m_Devs.get(id);
        }
        catch(Exception e)
        {
        	stopSelf();
        }
        if(idev.Open(true) == false)
        {
        	stopSelf();
        }
        screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CM S Pen Add-on");
        timer = new Timer();
        if(pref.getBoolean("detached", false))
        	polling = DET_POLLING;
        timer.schedule(new SPenTask(), polling);
	}
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
    	events.Release();
    	if(pref.getBoolean("soffchk", false))
    		if(mReceiver != null)
    			unregisterReceiver(mReceiver);
    	timer.cancel();
    	if(screenLock.isHeld())
    		screenLock.release();
    }
	
	class SPenTask extends TimerTask
	{
		public void run() {
    		try{
    			if (idev.getPollingEvent() == 0) {
    				if(idev.getSuccessfulPollingType() == 5 && idev.getSuccessfulPollingCode() == 14)
    				{
    					if(idev.getSuccessfulPollingValue() == 1)
    					{
    						screenLock.acquire();
    						i.putExtra("penInsert", false);
    						polling = DET_POLLING;
    						editor.putBoolean("detached", true);
    						editor.commit();
    						sendStickyBroadcast(i);
    						v.vibrate(75);
    						screenLock.release();
    					}
    					if(idev.getSuccessfulPollingValue() == 0)
    					{
    						i.putExtra("penInsert", true);
    						polling = pvalues[pref.getInt("polling", 0)];
    						editor.putBoolean("detached", false);
    						editor.commit();
    						sendStickyBroadcast(i);
    						v.vibrate(75);
    					}
    				}
    			}
    		}
    		catch(Exception e)
    		{
    			stopSelf();
    		}
    		timer.schedule(new SPenTask(), polling);
        }
	}
}
