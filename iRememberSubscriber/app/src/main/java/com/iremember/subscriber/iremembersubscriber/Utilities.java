package com.iremember.subscriber.iremembersubscriber;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class Utilities {

    public static final String CHANNEL_ID = "com.iremember.subscriber";
    public static final String CHANNEL_NAME = "iRemember";

    public static void createNotification(String title, Context context){

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel
                    (CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID);

        mNotificationManager.notify(0, mBuilder.build());
    }
}
