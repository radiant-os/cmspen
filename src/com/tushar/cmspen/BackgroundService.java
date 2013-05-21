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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class BackgroundService extends Service {
	BroadcastReceiver mReceiver;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate()
	{
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        getApplicationContext().startService(new Intent(getApplicationContext(),SPenDetection.class));
	}
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
    	if(mReceiver != null)
    		unregisterReceiver(mReceiver);
    	getApplicationContext().stopService(new Intent(getApplicationContext(),SPenDetection.class));
    }
}
