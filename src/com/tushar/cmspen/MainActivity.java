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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.pocketmagic.android.eventinjector.Events;
import net.pocketmagic.android.eventinjector.Events.InputDevice;

public class MainActivity extends Activity {
    int id = -1;
    boolean compatible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (pref.getInt("id", id) == -1) {
            try {
                compatible = new CheckComp().execute().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (compatible) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("id", id);
                editor.apply();
            } else {
                AlertDialog.Builder builderdonate = new AlertDialog.Builder(this);
                builderdonate.setTitle("CM S Pen Add-on");
                builderdonate.setMessage("Sorry your device is not compatible with this Application.");
                builderdonate.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                builderdonate.setCancelable(false);
                builderdonate.show();
            }
        }
        ToggleButton startStop = (ToggleButton) findViewById(R.id.onOfftoggle);
        if (pref.getBoolean("enabled", false)) {
            startStop.setChecked(true);
            StopEventMonitor(MainActivity.this);
            StartEventMonitor(MainActivity.this);
        }
        startStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    StartEventMonitor(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Detection Enabled", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("enabled", true);
                    editor.apply();
                } else {
                    Toast.makeText(MainActivity.this, "Detection Disabled", Toast.LENGTH_SHORT).show();
                    StopEventMonitor(MainActivity.this);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("enabled", false);
                    editor.apply();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void StopEventMonitor(Context ctx) {
        ctx.stopService(new Intent(ctx, com.tushar.cmspen.SPenDetection.class));
    }

    public static void StartEventMonitor(Context ctx) {
        ctx.startService(new Intent(ctx, com.tushar.cmspen.SPenDetection.class));
    }

    class CheckComp extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
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
            for (InputDevice idev : events.m_Devs) {
                {
                    try {
                        if (idev.Open(true))
                        if (idev.getName().contains("sec_e-pen")) {
                            temp = true;
                            id = events.m_Devs.indexOf(idev);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return temp;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            try {
                mDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
