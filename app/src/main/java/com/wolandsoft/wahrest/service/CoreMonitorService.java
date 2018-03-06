/*
    Copyright 2018 Alexander Shulgin

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
package com.wolandsoft.wahrest.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.wolandsoft.wahrest.R;
import com.wolandsoft.wahrest.activity.MainActivity;
import com.wolandsoft.wahrest.common.KeySharedPreferences;
import com.wolandsoft.wahrest.common.LogEx;

public class CoreMonitorService extends Service {
    private final int SERVICE_NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "Default";
    private BroadcastReceiver mReceiver;
    MessageListener mMessageListener;
    Message mMessage;

    private boolean mIsHomeWifiConnected = false;

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(this);
        KeySharedPreferences ksPref = new KeySharedPreferences(shPref, this);
        final String homePin = ksPref.getString(R.string.pref_home_pin_key, (Integer) null);
        final String wifiSsid = ksPref.getString(R.string.pref_wifi_ssid_key, (Integer) null);
        final String firstInREST = ksPref.getString(R.string.pref_first_in_rest_api_key, (Integer) null);
        final String lastOutREST = ksPref.getString(R.string.pref_last_out_rest_api_key, (Integer) null);
        if (homePin == null || homePin.isEmpty()
                || wifiSsid == null || wifiSsid.isEmpty()
                || firstInREST == null || firstInREST.isEmpty()
                || lastOutREST == null || lastOutREST.isEmpty()
                || !PermissionsManager.sPermissionGranted(this)) {
            LogEx.d("Incomplete configuration");
            this.stopSelf();
            return;
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    WifiManager wifiManager = (WifiManager) CoreMonitorService.this.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        String ssid = wifiInfo.getSSID();
                        LogEx.d("SSID ", ssid);
                        if (wifiSsid.equals(ssid) && !mIsHomeWifiConnected) {
                            mIsHomeWifiConnected = true;
                            onHomeWifiConnected();
                        }
                    } else if (wifiInfo.getSupplicantState() == SupplicantState.DISCONNECTED) {
                        LogEx.d("SSID ---");
                        if (mIsHomeWifiConnected) {
                            mIsHomeWifiConnected = false;
                            onHomeWifiDisconnected();
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                LogEx.d("Found message: " + new String(message.getContent()));
            }

            @Override
            public void onLost(Message message) {
                LogEx.d("Lost sight of message: " + new String(message.getContent()));
            }
        };

        mMessage = new Message("Hello World".getBytes());
        Nearby.getMessagesClient(this).publish(mMessage);
        Nearby.getMessagesClient(this).subscribe(mMessageListener);


        startForeground(SERVICE_NOTIFICATION_ID, buildStatusbarNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mMessage != null) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
        }
        if (mMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildStatusbarNotification() {
        Class<? extends Activity> activityCls = MainActivity.class;
        ComponentName component = new ComponentName(this, MainActivity.class);
        Intent showIntent = Intent.makeRestartActivityTask(component);
        showIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, showIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(this.getText(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_notif);
        //Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), iconResId);
        //builder.setLargeIcon(icon);
        builder.setContentIntent(contentIntent);
        builder.setWhen(0);
        builder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
            builder.setChannelId(CHANNEL_ID);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(Color.GRAY);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            builder.setLocalOnly(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_LOW);
            return builder.build();
        } else {
            return builder.getNotification();
        }
    }

    private void onHomeWifiConnected() {
        LogEx.d("onHomeWifiConnected()");
    }

    private void onHomeWifiDisconnected() {
        LogEx.d("onHomeWifiDisconnected()");
    }
}
