package com.iremember.subscriber.iremembersubscriber;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.content.ContentValues.TAG;

/**
 * Created by KEJ on 2018-02-26.
 */

public class CommandListenerService extends Service {

    private String channelId = "iRemember";
    private int notificationId = 1;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "Step2");

        Context context = getApplicationContext();
        CharSequence text = "Hello toast from CommandListenerService!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        String message = intent.getStringExtra(Intent.EXTRA_TEXT);
        Notification notification;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d(TAG, "Versison 26");
            notification = new Notification.Builder(CommandListenerService.this)
                    .setContentTitle("New Message")
                    .setContentText("You've received new messages.")
                    .setSmallIcon(R.drawable.lamp)
                    .setChannelId(channelId)
                    .build();
        }else{
            Log.d(TAG, "Not version 26");
            notification = new Notification.Builder(CommandListenerService.this)
                    .setContentTitle("New Message")
                    .setContentText("You've received new messages.")
                    .setSmallIcon(R.drawable.lamp)
                    .build();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
