package com.iremember.subscriber.iremembersubscriber;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by KEJ on 2018-03-05.
 */

public class Utilities {

    public static void createNotification(String message, String cText, String channelId, int notificationId, Context context){
        Notification notification;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d(TAG, "Versison 26");
            notification = new Notification.Builder(context)
                    .setContentTitle("New Message")
                    .setContentText("Hi :) You've received new messages.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId(channelId)
                    .build();
        }else{
            Log.d(TAG, "Not version 26");
            notification = new Notification.Builder(context)
                    .setContentTitle(message)
                    .setContentText(cText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, notification);

    }

}
