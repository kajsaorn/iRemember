package com.iremember.subscriber.iremembersubscriber.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.Command;
import com.iremember.subscriber.iremembersubscriber.Constants.Protocol;
import com.iremember.subscriber.iremembersubscriber.Constants.TimerConstants;
import com.iremember.subscriber.iremembersubscriber.ReminderActivity;
import com.iremember.subscriber.iremembersubscriber.Utils.BroadcastUtils;
import com.iremember.subscriber.iremembersubscriber.Utils.NotificationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkService extends Service {

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mServiceResolver;
    private String mServiceName, mRoomName;
    private ConnectionHandler mConnectionHandler;
    private NotificationUtils mNotificationManager;
    private boolean mServiceIsFound;

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand()");

        mRoomName = intent.getStringExtra(Broadcast.ROOM_NAME);
        mServiceName = intent.getStringExtra(Broadcast.SERVICE_NAME);

        if (mRoomName == null) {
            BroadcastUtils.broadcastAction(Broadcast.MISSING_ROOM_NAME, getApplicationContext());
        }
        if (mServiceName == null) {
            BroadcastUtils.broadcastAction(Broadcast.MISSING_SERVICE_NAME, getApplicationContext());
        }

        startServiceDiscovery();
        setServiceDiscoveryTimer();
        setupNotificationManager();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        log("onDestroy()");
        if (mConnectionHandler != null) {
            try {
                mConnectionHandler.closeConnection();
                mConnectionHandler.interrupt();
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.DISCONNECTION_FAILURE, getApplicationContext());
            }
        }
        BroadcastUtils.broadcastAction(Broadcast.DISCONNECTION_SUCCESS, getApplicationContext());
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupNotificationManager() {
        log("NotificationManager created foreground thread and notification.");
        mNotificationManager = new NotificationUtils(this);
        mNotificationManager.createNotificationForeground("Foreground", getApplicationContext(), this);
    }

    private void startServiceDiscovery() {
        mServiceIsFound = false;
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeServiceResolver();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(Protocol.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    private void stopServiceDiscovery() {
        log("Stop Service Discovery");

        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_DONE, getApplicationContext());

        if (!mServiceIsFound) {
            BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
        }
    }

    private void setServiceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                stopServiceDiscovery();
            }
        };
        new Timer().schedule(task, TimerConstants.DISCOVERY_DURATION);
    }

    public void initializeDiscoveryListener() {

        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                log("Started Service Discovery: " + mServiceName);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                if (service.getServiceType().equals(Protocol.SERVICE_TYPE)) {
                    mNsdManager.resolveService(service, mServiceResolver);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
                BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeServiceResolver() {
        mServiceResolver = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                String serviceName = serviceInfo.getServiceName();

                if (mServiceName != null && mServiceName.equals(serviceName)) {
                    log("Resolving service with correct name: " + serviceName);
                    mServiceIsFound = true;
                    startConnection(serviceInfo.getHost(), serviceInfo.getPort());
                } else {
                    log("Resolving service with incorrect name: " + serviceName);
                    BroadcastUtils.broadcastString(Broadcast.SERVICE_NAME, serviceName, getApplicationContext());
                }
            }
        };
    }

    private void startConnection(InetAddress host, int port) {
        try {
            log("Start connection");
            mConnectionHandler = new ConnectionHandler(host, port);
            mConnectionHandler.start();
        } catch (SocketException e) {
            e.printStackTrace();
            BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
        }
    }

    public void log(String msg) {
        Log.d("NetworkService", msg);
    }





    private class ConnectionHandler extends Thread {

        private DatagramSocket mDatagramSocket;
        private InetAddress mRemoteHost;
        private int mRemotePort;

        byte[] mWriteBuffer, mReadBuffer;
        DatagramPacket mWritePacket, mReadPacket;
        boolean hasRecentlyReceivedCommand;

        public ConnectionHandler(InetAddress host, int port) throws SocketException {
            log("Creating ConnectionHandler");
            mRemoteHost = host;
            mRemotePort = port;
            mDatagramSocket = new DatagramSocket(0);
        }

        public void closeConnection() throws IOException {
            sendUnregistrationMessage();
            mDatagramSocket.close();
        }

        public void run() {
            String mReadMessage;
            mReadBuffer = new byte[256];
            hasRecentlyReceivedCommand = false;

            acquireWiFiLock();

            try {
                sendRegistrationMessage();
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
            }

            while (true) {
                try {
                    mReadPacket = new DatagramPacket(mReadBuffer, mReadBuffer.length);
                    mDatagramSocket.receive(mReadPacket);

                    if (hasRecentlyReceivedCommand) {
                        log("Ignoring incoming message because previously received one.");
                        continue;
                    }

                    mReadMessage = new String(mReadPacket.getData(), 0, mReadPacket.getLength());
                    handleMessage(mReadMessage);
                    sendConfirmationMessage();

                } catch (Exception e) {
                    e.printStackTrace();
                    BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                }
            }
        }

        private void sendRegistrationMessage() throws IOException {
            log("Sending register message");
            mWriteBuffer = (Protocol.MESSAGE_REGISTER_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, mRemoteHost, mRemotePort);
            mDatagramSocket.send(mWritePacket);
            log("Done sending register message");
        }

        private void sendUnregistrationMessage() throws IOException {
            log("Sending unregister message");
            mWriteBuffer = (Protocol.MESSAGE_UNREGISTER_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, mRemoteHost, mRemotePort);
            mDatagramSocket.send(mWritePacket);
            log("Done sending unregister message");
        }

        private void sendConfirmationMessage() throws IOException {
            log("Sending confirmation message");
            mWriteBuffer = (Protocol.MESSAGE_CONFIRMATION_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, mRemoteHost, mRemotePort);
            mDatagramSocket.send(mWritePacket);
            log("Done sending confirmation message");
        }

        private void acquireWiFiLock() {
            WifiManager.WifiLock mWifiLock = null;
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                mWifiLock = wifiManager.createWifiLock("0 Backup wifi lock");
                mWifiLock.acquire();
            }
        }

        private void turnScreenOn(){
            //log("turnScreenOn()");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");
            wl.acquire();
        }

        private void setRecentlyReceived(boolean hasReceived) {
            hasRecentlyReceivedCommand = hasReceived;
            if (hasReceived) {
                setRecentlyReceivedTimer();
            }
        }

        private void setRecentlyReceivedTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    setRecentlyReceived(false);
                }
            };
            new Timer().schedule(task, TimerConstants.COMMAND_DURATION);
        }

        private void handleMessage(String message) {
            switch (message) {
                case Command.BREAKFAST:
                    turnScreenOn();
                    setRecentlyReceived(true);
                    startReminderActivity(message);
                    break;
                case Command.LUNCH:
                    turnScreenOn();
                    setRecentlyReceived(true);
                    startReminderActivity(message);
                    break;
                case Command.DINNER:
                    turnScreenOn();
                    setRecentlyReceived(true);
                    startReminderActivity(message);
                    break;
                case Protocol.CONNECTION_CONFIRMATION:
                    BroadcastUtils.broadcastAction(Broadcast.CONNECTION_SUCCESS, getApplicationContext());
                    break;
            }
        }

        private void startReminderActivity(String message) {
            Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Command.MESSAGE, message);
            startActivity(intent);
        }
    }



    /*

    private CommandReceiver mCommandReceiver;
    private NotificationUtils mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mCommandReceiver = new CommandReceiver();
        mNotificationManager = new NotificationUtils(this);
        log("onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tells the system this is a foreground service
        mNotificationManager.createNotificationForeground("Foreground", getApplicationContext(), this);
        mCommandReceiver.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterCommandReceiver();
        super.onDestroy();
    }

    private void unregisterCommandReceiver() {
        mCommandReceiver.interrupt();
        mCommandReceiver = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    */






     //The CommandReceiver is a thread that listens for incoming UDP messages on the network.
    /*
    private class CommandReceiver extends Thread {

        private DatagramSocket socket;
        private int port = 12345;
        private boolean recentlyReceived = false;

        public CommandReceiver() {

            log("CommandReceiver");
            try {
                // For DatagramSocket
                socket = new DatagramSocket(port);

            } catch (SocketException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void finnishConnection(){
            socket.close();
        }

        public void run() {
            DatagramPacket packetReceived;
            DatagramPacket packetSend;
            InetAddress masterInetAddress;
            int masterPort;
            byte[] readBuffer = new byte[256];
            String command;
            byte[] sendBuffer;

            acquireWiFiLock();
            //acquireMultiCastLock();

            while (true) {
                try {
                    packetReceived = new DatagramPacket(readBuffer, readBuffer.length);
                    socket.receive(packetReceived);

                    // If a command is not recently received, then do the command and answer
                    if (!recentlyReceived) {
                        turnScreenOn();
                        setRecentlyReceived(true);
                        setRecentlyReceivedTimer();
                        command = new String(packetReceived.getData(), 0,
                                packetReceived.getLength());
                        validateCommand(command);

                        // Sending packet with room id
                        masterInetAddress = packetReceived.getAddress();
                        masterPort = packetReceived.getPort();
                        String mRoomName = PreferenceUtils.readRoomName(getApplicationContext());
                        sendBuffer = mRoomName.getBytes();
                        packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                                masterInetAddress, masterPort);
                        socket.send(packetSend);
                        log("After validating command");
                    } else {
                        log("Command are recently received");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                }
            }
        }

        private synchronized void setRecentlyReceived(boolean received){
            recentlyReceived = received;

        }

        private void setRecentlyReceivedTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    setRecentlyReceived(false);
                }
            };
            new Timer().schedule(task, TimerConstants.COMMAND_DURATION);
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

    private void turnScreenOn(){
        log("turnScreenOn()");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");
        wl.acquire();
    }

    private void acquireWiFiLock() {
        WifiManager.WifiLock _wifiLock = null;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            log("wifiManager not null; " + wifiManager.toString());
            _wifiLock = wifiManager.createWifiLock("0 Backup wifi lock");
            _wifiLock.acquire();
            log("_wifiLock.isHeld() = " + _wifiLock.isHeld());
        }
    }


    private void acquireMultiCastLock(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("lock");
        multicastLock.acquire();
    }
*/
}
