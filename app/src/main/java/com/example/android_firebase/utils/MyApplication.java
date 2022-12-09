package com.example.android_firebase.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.android_firebase.R;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "channel_id_push";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannelId();
    }

    private void createChannelId() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "PushNotification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
