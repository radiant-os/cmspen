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
import net.pocketmagic.android.eventinjector.Events.InputDevice;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	int id = -1;
	boolean compatible = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(pref.getInt("id", id) == -1)
        {
        	try {
        		compatible = new CheckComp().execute().get();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	if(compatible)
        	{
        		SharedPreferences.Editor editor = pref.edit();
        		editor.putInt("id", id);
        		editor.commit();
        	}
        	else
        	{
        		AlertDialog.Builder builderdonate = new AlertDialog.Builder(this);
        		builderdonate.setTitle("CM S Pen Add-on");
        		builderdonate.setMessage("Sorry your device is not compatible with this Application. Please email me for further assistance.");
        		builderdonate.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
        				finish();
        			}
        		});
        		builderdonate.setCancelable(false);
        		builderdonate.show();
        	}
        }
        final int pvalues[] = {100,250,500,750,1000,1250,1500,1750,2000,2250,2500,2750,3000,3250,3500,3750,4000};
        final TextView polltext = (TextView) findViewById(R.id.pollingvalue);
        final TextView soffpolltext = (TextView) findViewById(R.id.soffpollingvalue);
        ToggleButton startStop = (ToggleButton) findViewById(R.id.onOfftoggle);
        final ToggleButton soffchk = (ToggleButton) findViewById(R.id.soffpollingtoggle);
        final SeekBar pvalue = (SeekBar) findViewById(R.id.polling);
        final SeekBar soffpvalue = (SeekBar) findViewById(R.id.soffpolling);
        if(pref.getBoolean("enabled", false))
        {
        	startStop.setChecked(true);
        }
        else
        {
        	soffchk.setEnabled(false);
        	pvalue.setEnabled(false);
        	soffpvalue.setEnabled(false);
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
        soffpvalue.setProgress(pref.getInt("soffpolling", 0));
        soffpolltext.setText("Screen-off Polling Value: " + String.valueOf(pvalues[pref.getInt("soffpolling", 0)]));
        startStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked)
                {
                	StartEventMonitor(MainActivity.this);
    				Toast.makeText(MainActivity.this, "Event monitor started.", Toast.LENGTH_SHORT).show();
                	SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("enabled", true);
    				editor.commit();
    				soffchk.setEnabled(true);
    	        	pvalue.setEnabled(true);
    	        	soffpvalue.setEnabled(true);
                }
                else
                {
                	Toast.makeText(MainActivity.this, "Event monitor stopped.", Toast.LENGTH_SHORT).show();
    				StopEventMonitor(MainActivity.this);
    				SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("enabled", false);
    				editor.commit();
    				soffchk.setEnabled(false);
    	        	pvalue.setEnabled(false);
    	        	soffpvalue.setEnabled(false);
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
    				StopEventMonitor(MainActivity.this);
    				StartEventMonitor(MainActivity.this);
                }
                else
                {
    				SharedPreferences.Editor editor = pref.edit();
    				editor.putBoolean("soffchk", false);
    				editor.commit();
    				soffpvalue.setEnabled(false);
    				StopEventMonitor(MainActivity.this);
    				StartEventMonitor(MainActivity.this);
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
				soffpolltext.setText("Screen-off Polling Value: " + String.valueOf(pvalues[arg1]));
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt("soffpolling", arg1);
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
	public static void StopEventMonitor(Context ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(pref.getBoolean("soffchk", false))
			ctx.stopService(new Intent(ctx,SPenDetection.class));
		else
			ctx.stopService(new Intent(ctx,BackgroundService.class));
	}
	
	public static void StartEventMonitor(Context ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(pref.getBoolean("soffchk", false))
			ctx.startService(new Intent(ctx,SPenDetection.class));
		else
			ctx.startService(new Intent(ctx,BackgroundService.class));
	}
	
	class CheckComp extends AsyncTask<Void, Void, Boolean> {
	    ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
		
	    @Override
		protected void onPreExecute()
	    {
	        mDialog.setMessage("             Checking Compatibility");
	    	mDialog.setProgressStyle(ProgressDialog.THEME_HOLO_DARK);
	        mDialog.setIndeterminate(true);
	        mDialog.setCancelable(false);
	        mDialog.show();
	    }

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Events events = new Events();
			events.Init();
			Boolean temp = false;
			for (InputDevice idev:events.m_Devs) {
	        	{
	        		try
	        		{
	        			if(idev.Open(true))
	        				if(idev.getName().contains("sec_e-pen") == true)
	        				{
	        					temp = true;
	        					id = events.m_Devs.indexOf(idev);
	        					break;
	        				}
	        		}
	        		catch(Exception e)
	        		{
	        			e.printStackTrace();
	        		}
	        	}
	        }
			return temp;
		}
		
		@Override
	    protected void onPostExecute(Boolean v) {
			mDialog.dismiss();
	    }
	}
}
