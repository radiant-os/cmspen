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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ScreenReceiver extends BroadcastReceiver {
 
    @Override
    public void onReceive(Context context, Intent intent) {
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = pref.edit();
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
        	if(pref.getBoolean("soffchk", false) == false)
        		context.stopService(new Intent(context,SPenDetection.class));
        	else
        	{
        		editor.putInt("polling", pref.getInt("soffpolling",8));
        		editor.commit();
        		if(pref.getBoolean("detached", false) == false)
        			{
        				SPenDetection.polling = SPenDetection.pvalues[pref.getInt("polling", 0)];
        			}
        	}
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
        	if(pref.getBoolean("soffchk", false) == false)
        		context.startService(new Intent(context,SPenDetection.class));
        	else
        	{
        		editor.putInt("polling", pref.getInt("pollingchoice",4));
        		editor.commit();
        		if(pref.getBoolean("detached", false) == false)
        			{
        				SPenDetection.polling = SPenDetection.pvalues[pref.getInt("polling", 0)];
        			}
        	}
        }
        else
        {
        	if(pref.getBoolean("enabled", false))
        	{
        		MainActivity.StopEventMonitor(context);
        		MainActivity.StartEventMonitor(context);
        		Alarm.startAlarm(context);
        	}
        }
    }
}