package com.iremember.subscriber.iremembersubscriber.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.Protocol;
import com.iremember.subscriber.iremembersubscriber.Constants.TimerConstants;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.ReminderActivity;
import com.iremember.subscriber.iremembersubscriber.StartActivity;
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
    private ConnectionHandler mConnectionHandler;
    private NotificationUtils mNotificationManager;
    private BroadcastReceiver mBroadcastReceiver;
    private WifiManager.WifiLock mWifiLock = null;

    private boolean isSearchingMasterService, isMasterServiceFound;
    private InetAddress mMasterServiceHost;
    private int mMasterServicePort;
    private String mMasterServiceName;
    private String mRoomName;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Started network service.");

        mRoomName = PreferenceUtils.readRoomName(getApplicationContext());
        mMasterServiceName = PreferenceUtils.readMasterServiceName(getApplicationContext());
        isSearchingMasterService = intent.getBooleanExtra(Broadcast.SEARCH_MASTER_SERVICE, false);
        isMasterServiceFound = false;

        if (isSearchingMasterService && mRoomName == null) {
            BroadcastUtils.broadcastAction(Broadcast.MISSING_ROOM_NAME, getApplicationContext());
        }
        if (isSearchingMasterService && mMasterServiceName == null) {
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
        closeConnection();
        removeWiFiLock();
        removeForeground();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Register broadcast receiver so that this
     * service starts listening to broadcast messages.
     */
    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new NetworkBroadcastReceiver();
        }
    }

    /**
     * Unregister broadcast receiver so that this
     * service stops listening to broadcast messages.
     */
    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * Make this service a foreground service by
     * setting up a notification manager.
     */
    private void setupNotificationManager() {
        mNotificationManager = new NotificationUtils(this);
        mNotificationManager.createNotificationForeground("Foreground", getApplicationContext(), this);
    }

    /**
     * Start service discovery. The network discovery is focused on either
     * finding a certain service by name or finding all available services,
     * depending on the boolean value of isSearchingMasterService.
     */
    private void startServiceDiscovery() {
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeServiceResolver();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(Protocol.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    /**
     * Stop service discovery.
     */
    private void stopServiceDiscovery() {
        log("Stopped service discovery");
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        if (isSearchingMasterService && !isMasterServiceFound) {
            BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
        }
        BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_DONE, getApplicationContext());
    }

    /**
     * Set timer to make sure the discovery finish after a certain time.
     */
    private void setServiceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                stopServiceDiscovery();
            }
        };
        new Timer().schedule(task, TimerConstants.DISCOVERY_DURATION);
    }

    /**
     * Initialize actions that will happen when a network service is discovered.
     */
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
                    if (isSearchingMasterService && service.getServiceName().equals(mMasterServiceName)) {
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

    /**
     * Initialize actions that will happen when a network
     * service matches a certain service name.
     */
    public NsdManager.ResolveListener initializeServiceResolver() {
        return new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                BroadcastUtils.broadcastAction(Broadcast.DISCOVERY_FAILURE, getApplicationContext());
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                isMasterServiceFound = true;
                mMasterServiceName = serviceInfo.getServiceName();
                mMasterServiceHost = serviceInfo.getHost();
                mMasterServicePort = serviceInfo.getPort();
                startConnection();
            }
        };
    }

    /**
     * Start socket connection to network service.
     */
    private void startConnection() {
        try {
            mConnectionHandler = new ConnectionHandler();
            mConnectionHandler.start();
            registerBroadcastReceiver();
        } catch (SocketException e) {
            e.printStackTrace();
            BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
        }
    }

    /**
     * Close socket connection to network service.
     */
    private void closeConnection() {
        if (mConnectionHandler != null) {
            mConnectionHandler.closeConnection();
            unregisterBroadcastReceiver();
        }
    }

    /**
     * Acquire wifi lock to prevent this service
     * from dozing off from the network.
     */
    private void acquireWiFiLock() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock("0 Backup wifi lock");
            mWifiLock.acquire();
        }
    }

    /**
     * Remove wifi lock to allow this service
     * to doze off from the network.
     */
    private void removeWiFiLock() {
        mWifiLock.release();
    }

    /**
     * Remove this service from the foreground.
     */
    private void removeForeground() {
        stopForeground(true);
    }


    public void log(String msg) {
        Log.d("NetworkService", msg);
    }


    /**
     *
     * The ConnectionHandler is a thread handling UDP connection to an
     * iRemember Master Service. This app is registered as a subscriber to the
     * service and will receive messages, e.g. that it is time for supper.
     *
     */
    private class ConnectionHandler extends Thread {

        private DatagramSocket mDatagramSocket;
        private DatagramPacket mWritePacket, mReadPacket;
        private byte[] mWriteBuffer, mReadBuffer;
        private boolean isConnected, isMessageRecentlyReceived, isRegistrationConfirmed;

        public ConnectionHandler() throws SocketException {
            mDatagramSocket = new DatagramSocket(0);
        }

        public void run() {
            log("Connection thread is running...");

            mReadBuffer = new byte[256];
            isMessageRecentlyReceived = false;
            isConnected = true;

            String[] data;
            InetAddress host;
            int port;
            String command, serviceName;

            sendRegistrationMessage();
            setRegistrationTimer();

            while (isConnected) {
                try {
                    mReadPacket = new DatagramPacket(mReadBuffer, mReadBuffer.length);
                    mDatagramSocket.receive(mReadPacket);

                    data = new String(mReadPacket.getData(), 0, mReadPacket.getLength()).split("\\$");

                    host = mReadPacket.getAddress();
                    port = mReadPacket.getPort();

                    command = "";
                    serviceName = "";

                    if (data.length > 1) {
                        command = data[0];
                        serviceName = data[1];
                    }

                    log("Received socket message from " + serviceName + ": " + command);

                    if (serviceName.equals(mMasterServiceName) && !isMessageRecentlyReceived) {
                        handleCommand(command, host, port);
                    }

                } catch (Exception e) {
                    Log.d("NetworkService", "Exception in thread while loop");
                    //e.printStackTrace();
                    if (isConnected) {
                        BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                    } else {
                        sendUnregistrationMessage();
                    }
                }
            }
        }

        public void closeConnection() {
            isConnected = false;
            mDatagramSocket.close();
        }

        private void handleCommand(String command, InetAddress host, int port) throws IOException {
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
                //e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
                Log.e("NetworkService", "Exception when sending registration message.");
            }
        }

        private void sendUnregistrationMessage() {
            try {
                mDatagramSocket = new DatagramSocket(0);
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
                //e.printStackTrace();
                Log.e("NetworkService", "Exception when sending confirmation message.");
            }
        }

        private void sendMessage(String message, InetAddress host, int port) throws IOException {
            log("Sending socket message: " + message);
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

    /**
     * BroadcastReceiver class that enables this service to receive broadcast messages.
     */
    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        public NetworkBroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cManager.getActiveNetworkInfo();
                Log.d("NetworkService", "Connectivity action: " + netInfo.getState());

                // If state is connected, make a new registration to master service.
                if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    //closeConnection();
                    //startConnection();
                }

            }
        }
    }
}
