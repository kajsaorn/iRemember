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
import java.util.Timer;
import java.util.TimerTask;

public class CommandHandler {

    private String mCommand;
    private Context mContext;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private DatagramSocket mDatagramSocket;
    private Object lock = new Object();

    public CommandHandler(String command, Context context) {
        mCommand = command;
        mContext = context;
        setupDeviceDiscovery();
        startDeviceDiscovery();
        setDeviceDiscoveryTimer();
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
                log("ip: " + service.getHost());
                if (service.getServiceName().startsWith(Protocol.SERVICE_PREFIX)){
                    //mNsdManager.resolveService(service, mResolveListener);
                    log("Service name starts with: " + Protocol.SERVICE_PREFIX);
                        mNsdManager.resolveService(service, new RoomFoundResolveListener());
                }
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

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                sendCommand(host, port);
            }
        };
    }

    private void sendCommand(InetAddress host, int port){
        log("sendCommand");
        log("host: " + host.getHostAddress());
        log("port: " + port);
        byte[] buffer;
        buffer = mCommand.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, host, port);
        try {
            log("before sending datagramPacket");
            mDatagramSocket.send(datagramPacket);
            log("after sending datagramPacket");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommandWithThread(InetAddress host, int port){
        new CommandSender(mCommand, host, port).start();
    }

    private void setDeviceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                stopDeviceDiscovery();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }

    private void stopDeviceDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        log("TIME IS UP!!!!");
    }

    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private class RoomFoundResolveListener implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            log("Failed resolve: " + nsdServiceInfo.getHost());
            log("Failed resolve: " + nsdServiceInfo.getServiceName());
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            log("onServiseResolved()");
            int port = serviceInfo.getPort();
            InetAddress host = serviceInfo.getHost();
            log("service name: " + serviceInfo.getServiceName());
            log("port: " + port);
            log("ip: " + host);
//            sendCommand(host, port);
            sendCommandWithThread(host, port);
        }
    }

}
