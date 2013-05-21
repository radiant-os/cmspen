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

import net.pocketmagic.android.eventinjector.Events;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Events.intEnableDebug(1);
        setContentView(R.layout.main);
        final int pvalues[] = {100,250,500,750,1000,1250,1500,1750,2000,2250,2500,2750,3000,3250,3500,3750,4000};
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final TextView polltext = (TextView) findViewById(R.id.pollingvalue);
        final TextView soffpolltext = (TextView) findViewById(R.id.soffpollingvalue);
        ToggleButton startStop = (ToggleButton) findViewById(R.id.onOfftoggle);
        ToggleButton soffchk = (ToggleButton) findViewById(R.id.soffpollingtoggle);
        SeekBar pvalue = (SeekBar) findViewById(R.id.polling);
        final SeekBar soffpvalue = (SeekBar) findViewById(R.id.soffpolling);
        if(pref.getBoolean("detection", false))
        {
        	startStop.setChecked(true);
        }
        if(pref.getBoolean("soffchk", false))
        {
        	soffchk.setChecked(true);
        }
        else
        {
        	soffpvalue.setEnabled(false);
        }
        if(pref.getInt("pollingchoice", -1) == -1)
        {
        	SharedPreferences.Editor editor = pref.edit();
			editor.putInt("pollingchoice", 4);
			editor.commit();
        }
        if(pref.getInt("soffpolling", -1) == -1)
        {
        	SharedPreferences.Editor editor = pref.edit();
			editor.putInt("soffpolling", 8);
			editor.commit();
        }
        pvalue.setProgress(pref.getInt("pollingchoice", 0));
        polltext.setText("Polling Value: " + String.valueOf(pvalues[pref.getInt("pollingchoice", 0)]));
        soffpvalue.setProgress(pref.getInt("soffpolling", 0)-8);
        soffpolltext.setText("Screen-off Polling Value: " + String.valueOf(pvalues[pref.getInt("soffpolling", 0)]));
        startStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked)
                {
                	StartEventMonitor();
    				Toast.makeText(MainActivity.this, "Event monitor started.", Toast.LENGTH_SHORT).show();
                	SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("enabled", true);
    				editor.commit();
                }
                else
                {
                	Toast.makeText(MainActivity.this, "Event monitor stopped.", Toast.LENGTH_SHORT).show();
    				StopEventMonitor();
    				SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("enabled", false);
    				editor.commit();
                }
            }
        });
        soffchk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked)
                {
                	SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("soffchk", true);
    				editor.commit();
    				soffpvalue.setEnabled(true);
    				StopEventMonitor();
    				StartEventMonitor();
                }
                else
                {
    				SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("soffchk", false);
    				editor.commit();
    				soffpvalue.setEnabled(false);
    				StopEventMonitor();
    				StartEventMonitor();
                }
            }
        });
        pvalue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				polltext.setText("Polling Value: " + String.valueOf(pvalues[arg1]));
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt("pollingchoice", arg1);
				editor.commit();
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(MainActivity.this, "Please disable and enable the detection to apply.", Toast.LENGTH_SHORT).show();
			}
        });
        soffpvalue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				soffpolltext.setText("Screen-off Polling Value: " + String.valueOf(pvalues[arg1+8]));
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt("soffpolling", arg1+8);
				editor.commit();
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(MainActivity.this, "Please disable and enable the detection to apply.", Toast.LENGTH_SHORT).show();
			}
        });
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
	public void StopEventMonitor() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(pref.getBoolean("soffchk", false))
			stopService(new Intent(this,SPenDetection.class));
		else
			stopService(new Intent(this,BackgroundService.class));
	}
	
	public void StartEventMonitor() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(pref.getBoolean("soffchk", false))
			startService(new Intent(this,SPenDetection.class));
		else
			startService(new Intent(this,BackgroundService.class));
	}
}
