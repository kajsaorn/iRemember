package com.iremember.subscriber.iremembersubscriber;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Commands;
import com.iremember.subscriber.iremembersubscriber.Constants.NetworkActions;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NetworkService extends Service {

    private final String TAG = "NetworkService";
    private String mServiceType = "_iremember._udp";
    private String mServiceName;

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private CommandReceiver mCommandReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeRegistrationListener();
        initializeCommandReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int port = mCommandReceiver.getPort();
        mServiceName = intent.getStringExtra("roomName");

        registerService(port);
        mCommandReceiver.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterCommandReceiver();
        unregisterService();
    }


    private void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(mServiceType);
        serviceInfo.setPort(port);
        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                broadcast(NetworkActions.CONNECTION_SUCCESS);
                //createNotification("Service registered", "registered", "iremember", 2, getApplicationContext());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                broadcast(NetworkActions.CONNECTION_FAILURE);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                broadcast(NetworkActions.DISCONNECTION_SUCCESS);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                broadcast(NetworkActions.DISCONNECT_FAILURE);
            }
        };
    }

    private void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }


    private void initializeCommandReceiver() {
        mCommandReceiver = new CommandReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void unregisterCommandReceiver() {
        mCommandReceiver.interrupt();
        mCommandReceiver = null;
    }

    private void unregisterService(){
        mNsdManager.unregisterService(mRegistrationListener);
    }

    /**
     * The CommandReceiver is a thread that listens for incoming UDP messages on the network.
     */
    private class CommandReceiver extends Thread {

        private DatagramSocket socket;
        private int port;

        public CommandReceiver() {
            try {
                socket = new DatagramSocket(0);
                port = socket.getLocalPort();
            } catch (SocketException e) {
                e.printStackTrace();
                broadcast(NetworkActions.SOCKET_FAILURE);
            }
        }


        public int getPort() {
            return port;
        }

        public void run() {
            DatagramPacket packet;
            byte[] readBuffer = new byte[256];
            String command;

            while (true) {
                try {
                    packet = new DatagramPacket(readBuffer, readBuffer.length);
                    socket.receive(packet);
                    command = new String(packet.getData(), 0, packet.getLength());
                    play(command);
                } catch (Exception e) {
                    e.printStackTrace();
                    broadcast(NetworkActions.SOCKET_FAILURE);
                }

            }
        }


        private void play(String command) {
            if (command.equals(Commands.BREAKFAST) ||
                    command.equals(Commands.LUNCH) ||
                    command.equals(Commands.DINNER )) {
                Intent reminderIntent = new Intent(getApplicationContext(), ReminderActivity.class);
                reminderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reminderIntent.putExtra("meal_command", command);
                startActivity(reminderIntent);
            }
        }
    }


    public void log(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
