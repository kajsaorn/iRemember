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
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

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
    private String mServiceName, mRoomName;
    private ConnectionHandler mConnectionHandler;
    private NotificationUtils mNotificationManager;
    WifiManager.WifiLock mWifiLock = null;
    private boolean isSearchingMasterService, isMasterServiceFound;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Started network service.");

        mRoomName = PreferenceUtils.readRoomName(getApplicationContext());
        mServiceName = PreferenceUtils.readMasterServiceName(getApplicationContext());
        isSearchingMasterService = intent.getBooleanExtra(Broadcast.SEARCH_MASTER_SERVICE, false);
        isMasterServiceFound = false;

        if (isSearchingMasterService && mRoomName == null) {
            BroadcastUtils.broadcastAction(Broadcast.MISSING_ROOM_NAME, getApplicationContext());
        }
        if (isSearchingMasterService && mServiceName == null) {
            BroadcastUtils.broadcastAction(Broadcast.MISSING_SERVICE_NAME, getApplicationContext());
        }

        acquireWiFiLock();
        startServiceDiscovery();
        setServiceDiscoveryTimer();
        setupNotificationManager();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        log("Destroying network service.");
        if (mConnectionHandler != null) {
            mConnectionHandler.closeConnection();
        }
        removeWiFiLock();
        removeForeground();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupNotificationManager() {
        mNotificationManager = new NotificationUtils(this);
        mNotificationManager.createNotificationForeground("Foreground", getApplicationContext(), this);
    }

    private void startServiceDiscovery() {
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeServiceResolver();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(Protocol.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    private void stopServiceDiscovery() {
        log("Stopped service discovery");
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        if (isSearchingMasterService && !isMasterServiceFound) {
            BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
        }
        BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_DONE, getApplicationContext());
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
                log("Service discovery started.");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                if (service.getServiceType().equals(Protocol.SERVICE_TYPE)) {
                    if (!isSearchingMasterService) {
                        BroadcastUtils.broadcastString(Broadcast.SERVICE_NAME, service.getServiceName(), getApplicationContext());
                    }
                    if (isSearchingMasterService && service.getServiceName().equals(mServiceName)) {
                        mNsdManager.resolveService(service, initializeServiceResolver());
                    }
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

    public NsdManager.ResolveListener initializeServiceResolver() {
        return new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                isMasterServiceFound = true;
                startConnection(serviceInfo.getHost(), serviceInfo.getPort());
            }
        };
    }

    private void startConnection(InetAddress host, int port) {
        try {
            mConnectionHandler = new ConnectionHandler(host, port);
            mConnectionHandler.start();
        } catch (SocketException e) {
            e.printStackTrace();
            BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
        }
    }

    private void acquireWiFiLock() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock("0 Backup wifi lock");
            mWifiLock.acquire();
        }
    }

    private void removeWiFiLock() {
        mWifiLock.release();
    }

    private void removeForeground() {
        stopForeground(true);
    }

    public void log(String msg) {
        Log.d("NetworkService", msg);
    }



    private class ConnectionHandler extends Thread {

        private DatagramSocket mDatagramSocket;
        private InetAddress mMasterHost;
        private int mMasterPort;
        private boolean isConnected;

        byte[] mWriteBuffer, mReadBuffer;
        DatagramPacket mWritePacket, mReadPacket;
        boolean isMessageRecentlyReceived, isRegistrationConfirmed;

        public ConnectionHandler(InetAddress host, int port) throws SocketException {
            mMasterHost = host;
            mMasterPort = port;
            mDatagramSocket = new DatagramSocket(0);
        }

        public void run() {
            log("Connection thread is running...");

            String mReadMessage;
            mReadBuffer = new byte[256];
            isMessageRecentlyReceived = false;
            isConnected = true;

            try {
                sendRegistrationMessage();
                setRegistrationTimer();
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
            }

            while (isConnected) {
                try {
                    mReadPacket = new DatagramPacket(mReadBuffer, mReadBuffer.length);
                    mDatagramSocket.receive(mReadPacket);

                    String ipSender = mReadPacket.getAddress().getHostAddress();
                    String ipMaster = mMasterHost.getHostAddress();

                    if (!ipSender.equals(ipMaster)) {
                        continue;
                    }
                    if (isMessageRecentlyReceived) {
                        continue;
                    }

                    handlePacket(mReadPacket);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (isConnected) {
                        BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                    } else {
                        BroadcastUtils.broadcastAction(Broadcast.DISCONNECTION_SUCCESS, getApplicationContext());
                    }
                }
            }

            try {
                sendUnregistrationMessage();
            } catch (IOException e) {
                e.printStackTrace();
                log("Unregistration failed");
            }
        }

        public void closeConnection() {
            isConnected = false;
            mDatagramSocket.close();
        }

        private void sendRegistrationMessage() throws IOException {
            log("Sending registration message: " + Protocol.MESSAGE_REGISTER_PREFIX + mRoomName);
            mWriteBuffer = (Protocol.MESSAGE_REGISTER_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, mMasterHost, mMasterPort);
            mDatagramSocket.send(mWritePacket);
        }

        private void sendUnregistrationMessage() throws IOException {
            log("Sending unregistration message: " + Protocol.MESSAGE_UNREGISTER_PREFIX + mRoomName);
            mDatagramSocket = new DatagramSocket(0);
            mWriteBuffer = (Protocol.MESSAGE_UNREGISTER_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, mMasterHost, mMasterPort);
            mDatagramSocket.send(mWritePacket);
            mDatagramSocket.close();
        }

        private void sendConfirmationMessage(InetAddress host, int port) throws IOException {
            log("Sending confirmation message: " + Protocol.MESSAGE_CONFIRMATION_PREFIX + mRoomName);
            mWriteBuffer = (Protocol.MESSAGE_CONFIRMATION_PREFIX + mRoomName).getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, host, port);
            mDatagramSocket.send(mWritePacket);
        }

        private void setRecentlyReceived(boolean isReceived) {
            isMessageRecentlyReceived = isReceived;
            if (isReceived) {
                setMessageReceivedTimer();
            }
        }

        private void setMessageReceivedTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    setRecentlyReceived(false);
                }
            };
            new Timer().schedule(task, TimerConstants.COMMAND_DURATION);
        }

        private void setRegistrationConfirmed(boolean isConfirmed) {
            isRegistrationConfirmed = isConfirmed;
        }

        private void setRegistrationTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    if (!isRegistrationConfirmed) {
                        BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
                    }
                }
            };
            new Timer().schedule(task, TimerConstants.REGISTRATION_DURATION);
        }

        private void handlePacket(DatagramPacket packet) throws IOException {
            String message = new String(mReadPacket.getData(), 0, mReadPacket.getLength());
            InetAddress host = packet.getAddress();
            int port = packet.getPort();

            log("Received socket message: " + message);

            switch (message) {
                case Command.BREAKFAST:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(message);
                    setReminderTimer();
                    break;
                case Command.LUNCH:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(message);
                    setReminderTimer();
                    break;
                case Command.DINNER:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(message);
                    setReminderTimer();
                    break;
                case Protocol.REGISTRATION_CONFIRMATION:
                    setRegistrationConfirmed(true);
                    BroadcastUtils.broadcastAction(Broadcast.CONNECTION_SUCCESS, getApplicationContext());
                    break;
            }
        }

        private void setReminderTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    stopReminderActivity();
                }
            };
            new Timer().schedule(task, TimerConstants.REMINDER_DURATION);
        }

        private void startReminderActivity(String message) {
            log("Starting reminder activity: " + message);
            Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Command.MESSAGE, message);
            startActivity(intent);
        }

        private void stopReminderActivity() {
            BroadcastUtils.broadcastAction(Broadcast.FINISH_ACTIVITY, getApplicationContext());
        }
    }
}
