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

import com.iremember.subscriber.iremembersubscriber.BuildConfig;
import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.Protocol;
import com.iremember.subscriber.iremembersubscriber.Constants.TimerConstants;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.ReminderActivity;
import com.iremember.subscriber.iremembersubscriber.ScreenSaverActivity;
import com.iremember.subscriber.iremembersubscriber.Utils.BroadcastUtils;
import com.iremember.subscriber.iremembersubscriber.Utils.NotificationUtils;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkService extends Service {

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
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
        log("Network service started.");

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
        registerBroadcastReceiver();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        log("Network service stopped.");
        closeConnection();
        removeWiFiLock();
        removeForeground();
        BroadcastUtils.broadcastAction(Broadcast.NETWORK_SERVICE_OFF, this);
        unregisterBroadcastReceiver();
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
            mBroadcastReceiver = new GlobalBroadcastReceiver();
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
        mNotificationManager.createNotificationForeground("iRemember", getApplicationContext(), this);
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
        log("Service discovery stopped.");
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
                String service = isSearchingMasterService ? mMasterServiceName : "All services";
                log("Service discovery started.");
                log("-> Searching for: " + service);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                log("-> Service found: " + service.getServiceName());

                if (service.getServiceType().equals(Protocol.SERVICE_TYPE)) {
                    if (isSearchingMasterService && service.getServiceName().equals(mMasterServiceName)) {
                        mNsdManager.resolveService(service, mResolveListener);
                    } else {
                        BroadcastUtils.broadcastString(Broadcast.SERVICE_NAME, service.getServiceName(), getApplicationContext());
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
    public void initializeServiceResolver() {
        mResolveListener = new NsdManager.ResolveListener() {

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
        }
    }

    private void startReminderActivity(String message) {
        Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Broadcast.MESSAGE, message);
        startActivity(intent);
    }

    private void stopReminderActivity() {
        BroadcastUtils.broadcastAction(Broadcast.FINISH_REMINDER, getApplicationContext());
    }

    private void startScreensaverActivity() {
        Intent intent = new Intent(getApplicationContext(), ScreenSaverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void stopScreensaverActivity() {
        BroadcastUtils.broadcastAction(Broadcast.FINISH_SCREENSAVER, getApplicationContext());
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
        mNotificationManager.clearNotifications();
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
            log("Connection started: " + mMasterServiceName);

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

                    log("Received message from " + serviceName);
                    log("-> " + command);

                    if (serviceName.equals(mMasterServiceName) && !isMessageRecentlyReceived) {
                        handleCommand(command, host, port);
                    }

                } catch (Exception e) {
                    if (isConnected) {
                        BroadcastUtils.broadcastAction(Broadcast.SOCKET_FAILURE, getApplicationContext());
                    } else {
                        sendUnregistrationMessage();
                        log("Connection stopped: " + mMasterServiceName);
                    }
                }
            }
        }

        /**
         * Close connection to the iRemember Master Service.
         * This will cause this thread to die.
         */
        public void closeConnection() {
            isConnected = false;
            mDatagramSocket.close();
        }

        /**
         * Handle command from iRemember Master Service.
         * @param command The command that describes what we should act upon.
         * @param host The host where the command came from.
         * @param port The port where the command came from.
         * @throws IOException
         */
        private void handleCommand(String command, InetAddress host, int port) throws IOException {
            switch (command) {
                case Protocol.COMMAND_COFFEE:
                    handleReminderCommand(UserMessage.REMINDER_COFFE, host, port);
                    break;
                case Protocol.COMMAND_MIDDAY:
                    handleReminderCommand(UserMessage.REMINDER_MIDDAY, host, port);
                    break;
                case Protocol.COMMAND_SUPPER:
                    handleReminderCommand(UserMessage.REMINDER_SUPPER, host, port);
                    break;
                case Protocol.COMMAND_REGISTERED:
                    handleRegistrationConfirmation();
                    break;
            }
        }

        /**
         * Handle a reminder command. The reminder activity will start and display the
         * provided reminder text. A confirmation message is sent to the host and port.
         */
        private void handleReminderCommand(String reminderText, InetAddress host, int port) {
            setRecentlyReceived(true);
            sendConfirmationMessage(host, port);

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hour >= TimerConstants.REMINDER_ALLOWED_START_HOUR
                    && hour < TimerConstants.REMINDER_ALLOWED_END_HOUR) {
                stopScreensaverActivity();
                startReminderActivity(reminderText);
                setReminderTimer();
            }
        }

        /**
         * Handle a registration confirmation. A connection success constant is broadcasted
         * to which ever activity is listening, in this case it will be the StartActivity,
         * which will in turn proceed to display the MainActivity.
         */
        private void handleRegistrationConfirmation() {
            setRegistrationConfirmed(true);
            BroadcastUtils.broadcastAction(Broadcast.CONNECTION_SUCCESS, getApplicationContext());
        }

        /**
         * Send a registration request message to the iRemember Master Service.
         * If the service received our request, we will get a confirmation message back.
         */
        private void sendRegistrationMessage() {
            try {
                log("-> Sending registration message to: " + mMasterServiceName);
                sendMessage(Protocol.REGISTER_PREFIX + mRoomName, mMasterServiceHost, mMasterServicePort);
            } catch (IOException e) {
                e.printStackTrace();
                BroadcastUtils.broadcastAction(Broadcast.CONNECTION_FAILURE, getApplicationContext());
                log("Exception when sending registration message.");
            }
        }

        /**
         * Send an unregistration message to the iRemember Master Service.
         * It will now know that we are no longer subscribing to reminder messages
         * and will not be worried if we do not send back message confirmations.
         */
        private void sendUnregistrationMessage() {
            try {
                log("-> Sending unregistration message to: " + mMasterServiceName);
                mDatagramSocket = new DatagramSocket(0);
                sendMessage(Protocol.UNREGISTER_PREFIX + mRoomName, mMasterServiceHost, mMasterServicePort);
                mDatagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                log("Exception when sending unregistration message.");
            }
        }

        /**
         * Send a confirmation message to the iRemember Master Service.
         * This indicates to the service that we have received and acted upon a message.
         * @param host The host to send confirmation message to.
         * @param port The port to send confirmation message to.
         */
        private void sendConfirmationMessage(InetAddress host, int port) {
            try {
                sendMessage(Protocol.CONFIRMATION_PREFIX + mRoomName, host, port);
            } catch (IOException e) {
                log("Exception when sending confirmation message.");
            }
        }

        /**
         * Send a message to some host.
         * @param host The host to send message to.
         * @param port The port to send message to.
         * @throws IOException
         */
        private void sendMessage(String message, InetAddress host, int port) throws IOException {
            mWriteBuffer = message.getBytes();
            mWritePacket = new DatagramPacket(mWriteBuffer, mWriteBuffer.length, host, port);
            mDatagramSocket.send(mWritePacket);

        }

        /**
         * To avoid socket being spammed, set boolean value to true if we recently
         * received a message. After some time this will go back to false.
         * @param isReceived
         */
        private void setRecentlyReceived(boolean isReceived) {
            isMessageRecentlyReceived = isReceived;
            if (isReceived) {
                setRecentlyReceivedTimer();
            }
        }

        /**
         * Set timer so that after a certain time we know that a message has not been
         * received recently, and our datagram socket will start acting on messages again.
         */
        private void setRecentlyReceivedTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    setRecentlyReceived(false);
                }
            };
            new Timer().schedule(task, TimerConstants.COMMAND_DURATION);
        }

        /**
         * Set boolean value to true if we received a registration
         * confirmed message from the iRemember Master Service.
         * @param isConfirmed
         */
        private void setRegistrationConfirmed(boolean isConfirmed) {
            isRegistrationConfirmed = isConfirmed;
        }

        /**
         * Set timer so that after a certain time we know that the iRemember Master has
         * not received or accepted our request to be registered as subscriber.
         */
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

        /**
         * Set time so that after a certain time the reminder is deactivated.
         */
        private void setReminderTimer() {
            TimerTask task = new TimerTask() {
                public void run() {
                    stopReminderActivity();
                }
            };
            new Timer().schedule(task, TimerConstants.REMINDER_DURATION);
        }
    }

    /**
     * BroadcastReceiver class that enables this service to receive broadcast messages.
     */
    private class GlobalBroadcastReceiver extends BroadcastReceiver {

        private static final String UNDEFINED = "$";
        private ConnectivityManager mConnectivityManager;
        private String mMasterNetworkName;
        private String mLatestNetworkName;

        public GlobalBroadcastReceiver() {
            mConnectivityManager = (ConnectivityManager) getApplication()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            mMasterNetworkName = networkInfo != null ? networkInfo.getExtraInfo() : UNDEFINED;
            mLatestNetworkName = networkInfo != null ? networkInfo.getExtraInfo() : UNDEFINED;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(android.content.Intent.ACTION_SCREEN_OFF);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    handleConnectivityChange();
                    break;
                case android.content.Intent.ACTION_SCREEN_OFF:
                    handleScreenOff();
                    break;
            }
        }

        private void handleConnectivityChange() {
            NetworkInfo mNewNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            String mNewNetworkName = mNewNetworkInfo != null ? mNewNetworkInfo.getExtraInfo() : UNDEFINED;

            if (mLatestNetworkName.equals(mMasterNetworkName) && !mNewNetworkName.equals(mMasterNetworkName)) {
                log("Handling connectivity change: Wrong WiFi");
                BroadcastUtils.broadcastAction(Broadcast.WRONG_WIFI, getApplicationContext());
            }
            if (!mLatestNetworkName.equals(mMasterNetworkName) && mNewNetworkName.equals(mMasterNetworkName)) {
                log("Handling connectivity change: Reconnect");
                closeConnection();
                startConnection();
            }
            mLatestNetworkName = mNewNetworkName;
        }

        private void handleScreenOff() {
            boolean isScreensaverAllowed = PreferenceUtils.readScreensaverAllowed(getApplicationContext());
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (isScreensaverAllowed
                    && hour >= TimerConstants.SCREENSAVER_ALLOWED_START_HOUR
                    && hour < TimerConstants.SCREENSAVER_ALLOWED_END_HOUR) {
                startScreensaverActivity();
            }
        }
    }
}
