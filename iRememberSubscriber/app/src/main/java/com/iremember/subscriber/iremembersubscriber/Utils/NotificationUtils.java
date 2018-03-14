package com.iremember.subscriber.iremembersubscriber.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;

public class NotificationUtils {

    private static final String CHANNEL_ID = "com.iremember.subscriber";
    private static final String CHANNEL_NAME = "iRemember";

    private NotificationManager mNotificationManager;
    private int mNotificationCount = 1;

    public NotificationUtils(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        log("NotificationUtils()");
    }

    public void createNotification(String title, Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel
                    (CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID);

        mNotificationManager.notify(mNotificationCount++, mBuilder.build());
    }

    public void createNotificationForeground(String title, Context context, NetworkService networkService){
        log("createNotificationForeground()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel
                    (CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID);
        log("createNotificationForeground(), before startForeground();");
        networkService.startForeground(mNotificationCount++, mBuilder.build());
        log("createNotificationForeground(), after startForeground();");
    }

    public void clearNotifications() {
        mNotificationManager.cancelAll();
    }

    public void log(String msg) {
        Log.d("NotificationUtils", msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
