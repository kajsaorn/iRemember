package com.iremember.subscriber.iremembersubscriber;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.content.ContentValues.TAG;
import static android.content.Context.WIFI_SERVICE;
import static com.iremember.subscriber.iremembersubscriber.Utilities.createNotification;

import com.iremember.subscriber.iremembersubscriber.Utilities;

/**
 * Created by KEJ on 2018-02-26.
 */

public class CommandListenerService extends Service {

    private String channelId = "iRemember";
    private int notificationId = 1;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart(Intent intent, int startId) {
        String message = intent.getStringExtra(Intent.EXTRA_TEXT);
        String cText = "Eating time";
        createNotification(message, cText, channelId, notificationId, (Context)this);

        // Waiting for messages...
        CommandReceiver receiver = new CommandReceiver(this);
        receiver.start();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CommandReceiver extends Thread{
        private DatagramSocket socket;
        private WifiManager.MulticastLock mLock;
        private Context mContext;

        public CommandReceiver(Context context){

            //mContext = context;
            mContext = getApplicationContext();
        }


        public void run(){
            DatagramPacket packet;
            String command;
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
 //           WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            mLock = wifiManager.createMulticastLock("lock");
            mLock.acquire();
            if(mLock.isHeld()){
                Log.d(TAG, "Lock is held" );
            }
            try {
//                socket = new DatagramSocket(12345);
                socket = new DatagramSocket(8888);
//                socket.bind(new InetSocketAddress(12345));
//                socket = new DatagramSocket(12345, InetAddress.getByName("255.255.255.255"));
//                socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
//                socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
//                socket = new DatagramSocket(12345);
                Log.d(TAG, "New socket created");
            } catch (SocketException e) {
                Log.d(TAG, "Ahh....nä...fel!!");
                e.printStackTrace();
            } /*catch (UnknownHostException e) {
                e.printStackTrace();
                Log.d(TAG, "Unknown host...");
            }*/

            byte[] readBuffer = new byte[256];
            int myInt = 2;
            while(1 < myInt){
                Log.d(TAG, "In while()");
                try{
                    packet = new DatagramPacket(readBuffer, readBuffer.length);
                    Log.d(TAG, "Waiting for paket...");
                    createNotification("Waiting for packet", "packetwait..", "iremember", 3, getApplicationContext());
                    socket.receive(packet);
                    Log.d(TAG, "Received paket...");
                    command = new String(packet.getData(), 0, packet.getLength());
                    play(command);
                }catch (Exception e){
                    Log.d(TAG, "Oh no...");
                }

            }
            mLock.release();
        }

        private void play(String command){
            Intent playerIntent = new Intent(mContext,Player.class);
            playerIntent.putExtra(Intent.EXTRA_TEXT, command);
            playerIntent.setType("text/plain");
            startActivity(playerIntent);
        }

    }
}
