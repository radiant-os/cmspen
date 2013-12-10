package com.tushar.cmspen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent i) {
		if(PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("enabled", false))
		{
			ctx.startService(new Intent(ctx,SPenDetection.class));
		}
	}

}
