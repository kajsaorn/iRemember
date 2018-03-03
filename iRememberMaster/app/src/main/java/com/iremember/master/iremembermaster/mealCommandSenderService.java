package com.iremember.master.iremembermaster;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static android.content.ContentValues.TAG;
import static android.content.Intent.getIntent;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by KEJ on 2018-02-27.
 */

public class mealCommandSenderService extends Service {
//    private String command;
    private String meal;
    private int port;
    private InetAddress inetAddress = null;

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*command = intent.getStringExtra(Intent.EXTRA_TEXT);
        Context context = getApplicationContext();
        CharSequence text = command;
        */
        Log.d(TAG, "onStartCommand()");
        Context context = getApplicationContext();
        Bundle extras = intent.getExtras();
//        Bundle extras = getIntent();
        meal = extras.getString("MEAL");
        Log.d(TAG, "mealCommandSenderServcice.onStartCommand().meal: " + meal);
        port = extras.getInt("PORT");
        Log.d(TAG, "mealCommandSenderServcice.onStartCommand().port: " + port);
        inetAddress = (InetAddress) extras.getSerializable("HOST");
//        Log.d(TAG, "inetAddress: " + inetAddress.toString());


        int duration = LENGTH_SHORT;
        Toast toast = Toast.makeText(context, meal, duration);
        toast.show();

        // Broadcasting
        Sender sender = new Sender(this);
        sender.start();

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Stopped mealCommandSenderService", LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class Sender extends Thread{
        private Context context;

        public Sender(Context context){
            this.context = context;
        }

        public void run(){
            DatagramSocket socket;
            DatagramPacket packet;
//            InetAddress inetAddress;

            long time = System.currentTimeMillis();
            byte[] buffer;
            try {
//                inetAddress = InetAddress.getByName("255.255.255.255");
                Log.d(TAG, "before creating DatagramSocket");
                socket = new DatagramSocket();  // ledig port
                Log.d(TAG, "after creating DatagramSocket");
                Log.d(TAG, "meal: " + meal);
                buffer = meal.getBytes();
                Log.d(TAG, "after meal.getBytes()");
                packet = new DatagramPacket(buffer,buffer.length, inetAddress, port);
                Log.d(TAG,"Before send");
                Toast.makeText(context, "Before sending packet", LENGTH_SHORT).show();
                socket.send(packet);
                Log.d(TAG,"After send");
//                Toast.makeText(context, "After sending packet", LENGTH_SHORT).show();

            } catch(Exception e) {
                Log.d(TAG,"Exception during send");
//                Toast.makeText(context, "Exception during send", LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
