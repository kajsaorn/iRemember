package com.iremember.subscriber.iremembersubscriber.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.Protocol;
import com.iremember.subscriber.iremembersubscriber.Constants.TimerConstants;
import com.iremember.subscriber.iremembersubscriber.R;
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
                startConnection(serviceInfo.getHost(), serviceInfo.getPort(), serviceInfo.getServiceName());
            }
        };
    }

    private void startConnection(InetAddress host, int port, String name) {
        try {
            mConnectionHandler = new ConnectionHandler(host, port, name);
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
        private InetAddress mMasterServiceHost;
        private int mMasterServicePort;
        private String mMasterServiceName;
        private boolean isConnected;

        byte[] mWriteBuffer, mReadBuffer;
        DatagramPacket mWritePacket, mReadPacket;
        boolean isMessageRecentlyReceived, isRegistrationConfirmed;

        public ConnectionHandler(InetAddress host, int port, String name) throws SocketException {
            mMasterServiceHost = host;
            mMasterServicePort = port;
            mMasterServiceName = name;
            mDatagramSocket = new DatagramSocket(0);
        }

        public void run() {
            log("Connection thread is running...");

            String mReadMessage;
            mReadBuffer = new byte[256];
            isMessageRecentlyReceived = false;
            isConnected = true;

            sendRegistrationMessage();
            setRegistrationTimer();

            while (isConnected) {
                try {
                    mReadPacket = new DatagramPacket(mReadBuffer, mReadBuffer.length);
                    mDatagramSocket.receive(mReadPacket);

                    String[] data = new String(mReadPacket.getData(), 0, mReadPacket.getLength()).split("//$");
                    InetAddress host = mReadPacket.getAddress();
                    int port = mReadPacket.getPort();

                    String command = "", serviceName = "";

                    if (data.length > 1) {
                        command = data[0];
                        serviceName = data[1];
                    }

                    if (serviceName.equals(mMasterServiceName) && !isMessageRecentlyReceived) {
                        handleCommand(command, host, port);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isConnected) {
                        BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                    } else {
                        sendUnregistrationMessage();
                    }
                } finally {
                    mDatagramSocket.close();
                }
            }
        }

        public void closeConnection() {
            isConnected = false;
        }

        private void handleCommand(String command, InetAddress host, int port) throws IOException {
            log("Handling socket command: " + command);

            switch (command) {
                case Protocol.COMMAND_COFFEE:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(getString(R.string.reminder_coffee));
                    setReminderTimer();
                    break;
                case Protocol.COMMAND_MIDDAY:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(getString(R.string.reminder_midday));
                    setReminderTimer();
                    break;
                case Protocol.COMMAND_SUPPER:
                    setRecentlyReceived(true);
                    sendConfirmationMessage(host, port);
                    startReminderActivity(getString(R.string.reminder_supper));
                    setReminderTimer();
                    break;
                case Protocol.COMMAND_REGISTERED:
                    setRegistrationConfirmed(true);
                    BroadcastUtils.broadcastAction(Broadcast.CONNECTION_SUCCESS, getApplicationContext());
                    break;
            }
        }

        private void sendRegistrationMessage() {
            try {
                sendMessage(Protocol.REGISTER_PREFIX + mRoomName, mMasterServiceHost, mMasterServicePort);
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
                Log.e("NetworkService", "Exception when sending registration message.");
            }
        }

        private void sendUnregistrationMessage() {
            try {
                sendMessage(Protocol.UNREGISTER_PREFIX + mRoomName, mMasterServiceHost, mMasterServicePort);
                BroadcastUtils.broadcastAction(Broadcast.DISCONNECTION_SUCCESS, getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.DISCONNECTION_FAILURE, getApplicationContext());
                Log.e("NetworkService", "Exception when sending unregistration message.");
            }
        }

        private void sendConfirmationMessage(InetAddress host, int port) {
            try {
                sendMessage(Protocol.CONFIRMATION_PREFIX + mRoomName, host, port);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("NetworkService", "Exception when sending confirmation message.");
            }
        }

        private void sendMessage(String message, InetAddress host, int port) throws IOException {
            mWriteBuffer = message.getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, host, port);
            mDatagramSocket.send(mWritePacket);
        }

        private void setRecentlyReceived(boolean isReceived) {
            isMessageRecentlyReceived = isReceived;
            if (isReceived) {
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

        private void setReminderTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    stopReminderActivity();
                }
            };
            new Timer().schedule(task, TimerConstants.REMINDER_DURATION);
        }

        private void stopReminderActivity() {
            BroadcastUtils.broadcastAction(Broadcast.FINISH_ACTIVITY, getApplicationContext());
        }

        private void startReminderActivity(String message) {
            Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Broadcast.MESSAGE, message);
            startActivity(intent);
        }
    }
}
