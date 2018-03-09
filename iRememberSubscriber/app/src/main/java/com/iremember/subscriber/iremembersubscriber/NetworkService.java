package com.iremember.subscriber.iremembersubscriber;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Constants.Command;
import com.iremember.subscriber.iremembersubscriber.Constants.Network;
import com.iremember.subscriber.iremembersubscriber.Constants.Protocol;
import com.iremember.subscriber.iremembersubscriber.Utils.BroadcastUtils;
import com.iremember.subscriber.iremembersubscriber.Utils.NotificationUtils;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NetworkService extends Service {

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private CommandReceiver mCommandReceiver;
    private NotificationUtils mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeRegistrationListener();
        mCommandReceiver = new CommandReceiver();
        mNotificationManager = new NotificationUtils(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int port = mCommandReceiver.getPort();
        registerService(port);
        mCommandReceiver.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterService();
        unregisterCommandReceiver();
        super.onDestroy();
    }

    private void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(Protocol.SERVICE_PREFIX + PreferenceUtils.readRoomName(this));
        serviceInfo.setServiceType(Protocol.SERVICE_TYPE);
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
                String serviceName = NsdServiceInfo.getServiceName();
                PreferenceUtils.writeRoomName(getApplicationContext(), serviceName);
                BroadcastUtils.broadcast(Network.CONNECTION_SUCCESS,getApplicationContext());
                mNotificationManager.createNotification(Network.CONNECTION_CONFIRMATION, getApplicationContext());
                log("Service is registered to network");
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                BroadcastUtils.broadcast(Network.CONNECTION_FAILURE, getApplicationContext());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                mNotificationManager.clearNotifications();
                BroadcastUtils.broadcast(Network.DISCONNECTION_SUCCESS, getApplicationContext());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                BroadcastUtils.broadcast(Network.DISCONNECTION_FAILURE, getApplicationContext());
            }
        };
    }

    private void unregisterCommandReceiver() {
        mCommandReceiver.interrupt();
        mCommandReceiver = null;
    }

    private void unregisterService(){
        mNsdManager.unregisterService(mRegistrationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                BroadcastUtils.broadcast(Network.SOCKET_FAILURE, getApplicationContext());
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
                    validateCommand(command);
                } catch (Exception e) {
                    e.printStackTrace();
                    BroadcastUtils.broadcast(Network.SOCKET_FAILURE, getApplicationContext());
                }
            }
        }

        private void validateCommand(String command) {
            if (
                    command.equals(Command.BREAKFAST) ||
                    command.equals(Command.LUNCH) ||
                    command.equals(Command.DINNER ))
            {
                Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("command", command);
                startActivity(intent);
            }
        }
    }

    public void log(String msg) {
        Log.d("NetworkService", msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
