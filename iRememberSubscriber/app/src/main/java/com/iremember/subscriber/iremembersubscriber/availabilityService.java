package com.iremember.subscriber.iremembersubscriber;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static android.content.ContentValues.TAG;
import static com.iremember.subscriber.iremembersubscriber.Utilities.createNotification;

public class AvailabilityService extends Service {
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private String mServiceName;
    private CommandReceiver mCommandReceiver;

    public AvailabilityService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String name = intent.getStringExtra("roomId");
        int port = initializeCommandReceiver();
        initializeRegistrationListener();
        mCommandReceiver.start();
        registerService(name, port);

        return START_NOT_STICKY;
    }

    private void registerService(String name, int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType("_iremember._udp");
        serviceInfo.setPort(port);
        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                createNotification("Service registered", "registered", "iremember", 2, getApplicationContext());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
      //  throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    private int initializeCommandReceiver(){
        mCommandReceiver = new CommandReceiver();
        return mCommandReceiver.getPort();
    }

    private class CommandReceiver extends Thread{
        private DatagramSocket socket;
        private Context mContext;
        private int port;

        public CommandReceiver(){
            try {
                socket = new DatagramSocket(0);
                port = socket.getPort();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            //mContext = context;
            mContext = getApplicationContext();
        }

        public int getPort(){
            return port;
        }

        public void run(){
            DatagramPacket packet;
            String command;
            byte[] readBuffer = new byte[256];
            int myInt = 2;

            while(true){
                try{
                    packet = new DatagramPacket(readBuffer, readBuffer.length);
                    createNotification("Waiting for packet", "packetwait..", "iremember", 3, getApplicationContext());
                    socket.receive(packet);
                    command = new String(packet.getData(), 0, packet.getLength());
                    play(command);
                }catch (Exception e){
                    Log.d(TAG, "Oh no...");
                }

            }
        }

        private void play(String command){
            Intent playerIntent = new Intent(mContext,PlayerActivity.class);
            playerIntent.putExtra("meal_command", command);
            playerIntent.setType("text/plain");
            startActivity(playerIntent);
        }

    }

}
