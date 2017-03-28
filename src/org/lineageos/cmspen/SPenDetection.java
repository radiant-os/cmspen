/**
 Code modifications to original Tushar Dudani source are released under the GPLv2
 Copyright 2015 CyanogenMod
 Copyright 2017 LineageOS
 **/

/**

 Original author:
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

package org.lineageos.cmspen;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.util.Log;

import net.pocketmagic.android.eventinjector.Events;
import net.pocketmagic.android.eventinjector.Events.InputDevice;

public class SPenDetection extends Service {

    public static final int TOUCH_BUTTON_PRESS = 1;
    public static final int TOUCH_BUTTON_LONG_PRESS = 2;
    public static final int HOVER_BUTTON_PRESS = 3;
    public static final int HOVER_BUTTON_LONG_PRESS = 4;
    public static final int HOVER_BUTTON_DOUBLE_PRESS = 5;

    public static final int VIBRATE_TIME = 75;

    // from kernel's /include/uapi/linux/input.h
    public static final int BTN_TOUCH = 0x14a;  //330
    public static final int BTN_STYLUS = 0x14b; //331
    public static final int SW_PEN_INSERT = 0x13; //19

    Events events = new Events();
    static Vibrator v;
    int id = -1;
    public static final int POLLING = 20;
    static Intent i = new Intent("com.samsung.pen.INSERT");
    static Intent SPen_Event = new Intent("com.tushar.cm_spen.SPEN_EVENT");
    static WakeLock screenLock;
    static InputDevice idev;
    static EventHandler h;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static void waitForEvent() {
        Log.d("CMSPen", "waitForEvent() called");
        h.sendEmptyMessage(0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        boolean compatible = false;
        try {
            compatible = new CheckComp().execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CMSPen", "Couldn't run compatibility test...");

        }
        if (!compatible) {
            stopSelf();
            Log.d("CMSPen", "Service stopped because not compatible");
        } else {
            Log.d("CMSPen", "Starting service...");
            events.Init();
            v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
            try {
                idev = events.m_Devs.get(id);
                if (!idev.Open(true)) {
                    Log.d("CMSPen", "Service stopped because Event file could not be opened.");
                    stopSelf();
                }
            } catch (Exception e) {
                stopSelf();
                Log.d("CMSPen", e.getMessage());
            }
            screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CMSPen");
            h = new EventHandler();
            new Thread() {
                @Override
                public void run() {
                    AddListener(id);
                }
            }.start();
        }
    }


    class CheckComp extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
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

        }
    }

    class EventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            new EventAsync().execute();
        }
    }

    class EventAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            boolean inserted = false;
            boolean sTouched = false;
            long pressTime = 0;
            int hPressCount = 0;
            long firstHoverPressTime = 0;
            while (true) {
                if (idev.getPollingEvent() == 0) {
                    if (idev.getSuccessfulPollingType() == 5 && (idev.getSuccessfulPollingCode() == 14 || idev.getSuccessfulPollingCode() == SW_PEN_INSERT)) {
                        if (idev.getSuccessfulPollingValue() == 1) {
                            i.putExtra("penInsert", false);
                            sendBroadcastAsUser(i, new UserHandle(UserHandle.USER_SYSTEM));
                            screenLock.acquire();
                            screenLock.release();
                            v.vibrate(VIBRATE_TIME);
                        }
                        if (idev.getSuccessfulPollingValue() == 0) {
                            i.putExtra("penInsert", true);
                            sendBroadcastAsUser(i, new UserHandle(UserHandle.USER_SYSTEM));
                            v.vibrate(VIBRATE_TIME);
                            inserted = true;
                        }
                    }
                    if (idev.getSuccessfulPollingType() == 0 && idev.getSuccessfulPollingCode() == 0 && idev.getSuccessfulPollingValue() == 0 && inserted) {
                        break;
                    }
                  //  if ((idev.getSuccessfulPollingType() !=0))
                  //      Log.d("CMSPen", (String.valueOf(idev.getSuccessfulPollingType()) + " " + String.valueOf(idev.getSuccessfulPollingCode()) + " " + String.valueOf(idev.getSuccessfulPollingValue())));
                    if (idev.getSuccessfulPollingType() == 1 && idev.getSuccessfulPollingCode() == BTN_TOUCH) {
                        if (idev.getSuccessfulPollingValue() == 1) {
                            sTouched = true;
                        }
                        if (idev.getSuccessfulPollingValue() == 0) {
                            sTouched = false;
                        }
                    }
                    if (idev.getSuccessfulPollingType() == 1 && idev.getSuccessfulPollingCode() == BTN_STYLUS) {
                        if (idev.getSuccessfulPollingValue() == 1) {
                            pressTime = System.currentTimeMillis();
                        }
                        if (idev.getSuccessfulPollingValue() == 0) {
                            long pressedFor = System.currentTimeMillis() - pressTime;
                            if (pressedFor >= 500) {
                                if (sTouched) {
                                    SPen_Event.putExtra("EVENT_CODE", TOUCH_BUTTON_LONG_PRESS);
                                    sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                    Log.d("CMSPen", "Touch Button Long Press");
                                } else {
                                    SPen_Event.putExtra("EVENT_CODE", HOVER_BUTTON_LONG_PRESS);
                                    sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                    Log.d("CMSPen", "Hover Button Long Press");
                                }
                            } else if (pressedFor >= 20) {
                                if (sTouched) {
                                    SPen_Event.putExtra("EVENT_CODE", TOUCH_BUTTON_PRESS);
                                    sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                    Log.d("CMSPen", "Touch Button Press");
                                } else {
                                    hPressCount++;
                                    if (hPressCount == 2) {
                                        long temp = System.currentTimeMillis() - firstHoverPressTime;
                                        Log.d("CMSPen", String.valueOf(temp));
                                        if (temp <= 1500) {
                                            SPen_Event.putExtra("EVENT_CODE", HOVER_BUTTON_DOUBLE_PRESS);
                                            sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                            Log.d("CMSPen", "Hover Button Double Press");
                                        } else {
                                            SPen_Event.putExtra("EVENT_CODE", HOVER_BUTTON_PRESS);
                                            sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                            Log.d("CMSPen", "Hover Button Press");
                                        }
                                        hPressCount = 0;
                                    } else {
                                        firstHoverPressTime = System.currentTimeMillis();
                                        SPen_Event.putExtra("EVENT_CODE", HOVER_BUTTON_PRESS);
                                        sendBroadcastAsUser(SPen_Event, new UserHandle(UserHandle.USER_SYSTEM));
                                        Log.d("CMSPen", "Hover Button Press");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    try {
                        SystemClock.sleep(POLLING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            new Thread() {
                @Override
                public void run() {
                    AddListener(id);
                }
            }.start();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (events != null) events.Release();
        if ((screenLock != null) && (screenLock.isHeld()))
            screenLock.release();
        if (v != null) v.cancel();
    }

    public int AddListener(int devid) {
        return AddFileChangeListener(devid);
    }

    private native int AddFileChangeListener(int devid);

    static {
        System.loadLibrary("SPenEventInjector");
    }
}
