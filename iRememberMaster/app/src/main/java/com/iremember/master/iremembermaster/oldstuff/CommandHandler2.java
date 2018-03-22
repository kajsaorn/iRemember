package com.iremember.master.iremembermaster.oldstuff;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Constants.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KEJ on 2018-03-07.
 */

public class CommandHandler2 {
    private String mCommand;
    private Context mContext;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private DatagramSocket mDatagramSocket;
    private Object lock = new Object();
    private LinkedList<NsdServiceInfo>  serviceInfos = new LinkedList<NsdServiceInfo>();

    public CommandHandler2(String command, Context context) {
        mCommand = command;
        mContext = context;
        setupDeviceDiscovery();
        startDeviceDiscovery();
//        setDeviceDiscoveryTimer();
        setAnotherDeviceDiscoveryTimer();
    }

    private void setupDeviceDiscovery() {
        try {
            mDatagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();
//        initializeResolveListener();
    }

    private void startDeviceDiscovery() {
        mNsdManager.discoverServices(Protocol.SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                log("Discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                log("A service was found: " + service.getServiceName());
                //log("ip: " + service.getHost());
              /*  if (service.getServiceName().startsWith(Protocol.SERVICE_PREFIX)){
                    //mNsdManager.resolveService(service, mResolveListener);
                    //log("Service name starts with: " + Protocol.SERVICE_PREFIX);
                    mNsdManager.resolveService(service,
                            new CommandHandler2.RoomFoundResolveListener());
                }
                */
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                log("Service lost: " + service.getServiceName());
                log("Service lost: " + service.getHost());
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                log("Discovery is stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void setDeviceDiscoveryTimer() {
        log("Discovery timer");
        TimerTask task = new TimerTask() {
            public void run() {
                stopDeviceDiscovery();
                sendCommandToAllInServiceInfos();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }

    private void sendCommandToAllInServiceInfos(){
        log("sendCommandToAllInSterviceInfor");
        byte[] buffer;
        buffer = mCommand.getBytes();

        while (!serviceInfos.isEmpty()) {
            NsdServiceInfo nsdServiceInfo = serviceInfos.removeFirst();
            InetAddress host = nsdServiceInfo.getHost();
            int port = nsdServiceInfo.getPort();
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, host, port);
            try {
                mDatagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mDatagramSocket.close();
        mDatagramSocket = null;
    }

    private void setAnotherDeviceDiscoveryTimer(){
        TimerTask task = new TimerTask() {
            public void run() {
                stopDeviceDiscovery();
                sendCommandToAllInServiceInfos();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }

    private void stopDeviceDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        log("TIME IS UP!!!!");
    }

    private synchronized void appendServiceInfo(NsdServiceInfo serviceInfo) {
        serviceInfos.add(serviceInfo);
    }

    private class RoomFoundResolveListener implements NsdManager.ResolveListener {

        public RoomFoundResolveListener() {
            log("Created ResolveListener");
        }

        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            //log("Failed resolve: " + nsdServiceInfo.getHost());
            log("Failed to resolve: " + nsdServiceInfo.getServiceName());
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            appendServiceInfo(serviceInfo);
        }
    }

    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

}
