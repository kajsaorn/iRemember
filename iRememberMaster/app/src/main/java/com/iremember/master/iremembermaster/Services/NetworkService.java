package com.iremember.master.iremembermaster.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.iremember.master.iremembermaster.CommandHandler;
import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Constants.Protocol;
import com.iremember.master.iremembermaster.Utils.NotificationUtils;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.iremember.master.iremembermaster.Constants.Protocol.SERVICE_TYPE;

public class NetworkService extends Service {
    private NotificationUtils mNotificationManager;
    private DatagramSocket datagramSocket;
    private int localPort;
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    WifiManager.WifiLock wifiLock = null;
    WifiManager wifiManager = null;
    WifiManager.MulticastLock wifiMulticastLock = null;

    public NetworkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = new NotificationUtils(this);
        try {
            datagramSocket = new DatagramSocket(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        String command = intent.getStringExtra(Command.NETWORKSERVICE_COMMAND);
        if (command.equals(Command.REGISTER_COMMAND)) {
            // Tell the system this is a foreground service
            registerTheService();
        } else {    // Then it must be a meal command
            new CommandHandler(command, getApplicationContext());
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNSD();
        closeDatagramSockets();
        removeWiFiLock();
        removeMulticastLock();
        removeForeground();
    }

    /**
     * Register the service (NSD)
     */
    private void registerTheService() {
        acquireWiFiLock();
        acquireMulticastLock();
        registerAsForegroundService();
        initializeSubscriberRegistrationSocket();
        startSubscriberRegistrationThread();
        initializeRegistrationListener();
        registerNSDService();
    }


    /**
     * Unregister NSD service so the master is not discoverable anymore
     */
    private void unregisterNSD() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    private void closeDatagramSockets() {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }

    private void removeWiFiLock() {
        wifiLock.release();
    }

    private void removeMulticastLock() {
        if (wifiMulticastLock != null) {
            wifiMulticastLock.release();
            wifiMulticastLock = null;
        }
    }

    private void removeForeground() {
        stopForeground(true);
    }

    /**
     * Tells the system that the app shall run as foreground service. This is necessary for
     * being able to receive search requests from subscribers.
     */
    private void registerAsForegroundService() {
        mNotificationManager.createNotificationForeground("Foreground", getApplicationContext(),
                this);
    }

    private void initializeSubscriberRegistrationSocket() {
        try {
            datagramSocket = new DatagramSocket();
            localPort = datagramSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
            // TODO what to do if not possible to create a socket...
        }
    }

    /**
     * Starts the thread that receive and manage registration requestst from receivers
     */
    private void startSubscriberRegistrationThread() {
        new CommandReceiver().start();
    }

    /**
     * Initialize registration listener for NSD registration
     */
    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
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

    /**
     * Register the system with NSD, to make it discoverable for subscribers
     */
    private void registerNSDService() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(PreferenceUtils.readMasterName(this));
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(localPort);

        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    private void acquireWiFiLock() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            wifiLock = wifiManager.createWifiLock("0 wifi lock");
            wifiLock.acquire();
        }
    }

    private void acquireMulticastLock() {
// Acquire multicast lock
        wifiMulticastLock = wifiManager.createMulticastLock("multicastLock");
        wifiMulticastLock.setReferenceCounted(true);
        wifiMulticastLock.acquire();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }


    /**
     * Internal class for managing subscriber registration and unregistration.
     */
    private class CommandReceiver extends Thread {
        boolean shallContinueListen = true;

        public void run() {
            DatagramPacket packetReceived;
            DatagramPacket packetSend;
            InetAddress subscriberInetAddress;

            byte[] readBuffer = new byte[256];
            String message;
            byte[] sendBuffer;

            while (shallContinueListen) {
                try {
                    // Receiving packet with containing registration/unregistration commands
                    packetReceived = new DatagramPacket(readBuffer, readBuffer.length);
                    /* For DatagramSocket */
                    datagramSocket.receive(packetReceived);

                    message = new String(packetReceived.getData(), 0,
                            packetReceived.getLength());
                    log("Recieved registration message from subscriber: " + message);
                    // Register the subscriber
                    boolean commandOk = registerOrUnregisterSubscriber(message, packetReceived);

                    // Send back answer
                    subscriberInetAddress = packetReceived.getAddress();
                    int subscriberPort = packetReceived.getPort();
                    if (commandOk) {
                        sendBuffer = (Protocol.REGISTRATION_CONFIRMATION +
                                PreferenceUtils.readMasterName(getApplicationContext())).getBytes();
                    } else {
                        sendBuffer = Protocol.REGISTRATION_REJECTED.getBytes();
                    }
                    packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                            subscriberInetAddress, subscriberPort);
                    log("sendingConfirmation " + new String(packetSend.getData(), 0, packetSend.getLength()));
                    datagramSocket.send(packetSend);
                } catch (Exception e) {
                   // Ending up here when the socket is closed -> stop while()-loop
                    shallContinueListen = false;
                    log("socket is closed");
                }
            }
        }

        /**
         * Register, unregister or update a subscribers data in the master
         * @param message
         * @param packetReceived
         */
        private boolean registerOrUnregisterSubscriber(String message, DatagramPacket packetReceived) {
            log("regissterOrUnregisterSubscriber()");
            log("message = " + message);
            String[] splitedMessage = message.split("\\$");
            String command = splitedMessage[0];
            log("command = " + command);
            String roomName = splitedMessage[1];
            log("roomName = " + roomName);
            String ip = packetReceived.getAddress().getHostAddress();
            log("ip = " + ip);
            String port = "" + packetReceived.getPort();
            log("port = " + port);

            if ((command != null) && command.equals(Protocol.REGISTER_PREFIX)) {
                PreferenceUtils.writeSubscriber(getApplicationContext(), roomName, (ip + "$" + port));
                return true;
            } else if ((command != null) && command.equals(Protocol.UNREGISTER_PREFIX)){
                PreferenceUtils.removeSubscriber(getApplicationContext(), roomName);
                return true;
            }
            return false;
        }
    }

    public void log(String msg) {
        Log.d("NetworkService", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

}
