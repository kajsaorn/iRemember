package com.iremember.master.iremembermaster;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by KEJ on 2018-03-06.
 */

public class NetworkHandler {
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private String mCommand;
    private DatagramSocket datagramSocket;
    private Context mContext;

    public NetworkHandler(String command, Context context) {
        log("context: " + context.toString());
        log("NetworkHandler." + command);
        mContext = context;
        mCommand = command;
        sendCommand(context);
    }

    private void sendCommand(Context context) {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        log("Created DatagramSocket");
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        log("After initializeResolveListener");
        initializeDiscoveryListener();
        log("After initializeDiscoveryListener");
        mNsdManager.discoverServices(Protocol.SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        log("discoverServices");

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
                sendMealMessage(host, port);

            }
        };
    }

    private void sendMealMessage(InetAddress host, int port){
        byte[] buffer;
        buffer = mCommand.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, host, port);
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                log("onDiscoveryStarted() " + regType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                log("Wow, service found :)" + service.getServiceName());
                // A service was found! Do something with it.
                if (!service.getServiceType().equals(Protocol.SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                } else if (service.getServiceName().startsWith(Protocol.SERVICE_IDENTIFIER)){
                     mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
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

    public void log(String msg) {
        Log.d("NetworkHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

}
