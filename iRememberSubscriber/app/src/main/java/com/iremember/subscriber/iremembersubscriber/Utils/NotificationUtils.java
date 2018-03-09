package com.iremember.subscriber.iremembersubscriber.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.iremember.subscriber.iremembersubscriber.R;

public class NotificationUtils {

    private static final String CHANNEL_ID = "com.iremember.subscriber";
    private static final String CHANNEL_NAME = "iRemember";

    private NotificationManager mNotificationManager;
    private int mNotificationCount;

    public NotificationUtils(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void clearNotifications() {
        mNotificationManager.cancelAll();
    }
}
