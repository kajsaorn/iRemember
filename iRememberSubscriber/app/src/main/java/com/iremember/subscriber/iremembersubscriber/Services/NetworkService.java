package com.iremember.subscriber.iremembersubscriber.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Constants.Command;
import com.iremember.subscriber.iremembersubscriber.Constants.Network;
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

    /**
     * The CommandReceiver is a thread that listens for incoming UDP messages on the network.
     */
    private class CommandReceiver extends Thread {

        private DatagramSocket socket;
        private int port = 12345;
        private boolean recentlyReceived = false;

        public CommandReceiver() {

            log("CommandReceiver");
            try {
                /* For DatagramSocket */
                socket = new DatagramSocket(port);

            } catch (SocketException e) {
                e.printStackTrace();
                BroadcastUtils.broadcast(Network.SOCKET_FAILURE, getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeSocket(){
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
                    // Receiving packet with command
                    packetReceived = new DatagramPacket(readBuffer, readBuffer.length);
                    /* For DatagramSocket */
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
                    BroadcastUtils.broadcast(Network.SOCKET_FAILURE, getApplicationContext());
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
            new Timer().schedule(task, TimerConstants.DURATION);
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

/*    private void acquireMultiCastLock(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("lock");
        multicastLock.acquire();
    }
*/
    public void log(String msg) {
        Log.d("NetworkService", msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
