package com.iremember.master.iremembermaster;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private DiscoveryListener mDiscoveryListener;
    private static final String TAG = "MainActivity";
    private static final String SERVICE_TYPE = "_iremember._udp.";
    private static final String mServiceName = "room";
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private EditText mLog;
    private String meal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLog = (EditText) findViewById(R.id.mEditText);
    }

    public void btnBreakfastOnClick(View v){
 /*       Intent commandSenderIntent = new Intent(this, mealCommandSenderService.class);
        commandSenderIntent.putExtra(Intent.EXTRA_TEXT, "Breakfast");
        commandSenderIntent.setType("text/plain");
        startService(commandSenderIntent);
        */

        InetAddress host = null;
        try {
            host = InetAddress.getByName("192.168.1.90");
            Log.d(TAG, "host: " + host.toString());
        } catch (UnknownHostException e) {
            Log.d(TAG, "Could not create host");
            e.printStackTrace();
        }
        meal = "Breakfast";
        sendMealMessage(host, 8888);
    }

    public void btnBreakfastNSDOnClick(View v){
        meal = "Breakfast";
        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                showToast("Service discovery started");
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                showToast("Service discovery success: " + service);
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    showToast("Unknown Service type: " + service.getServiceType());
                    showToast("Searched service type: " + SERVICE_TYPE);
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    showToast("Same machine: +" + mServiceName);
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("room")){
//                    mNsdManager.resolveService(service, mResolveListener);
                    showToast("Wow, servie found: " + service.getServiceName());
                    Log.d(TAG, "Wow, service found: " + service.getServiceName());
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
//                mService = serviceInfo;
//                int port = mService.getPort();
                int port = serviceInfo.getPort();
//                InetAddress host = mService.getHost();
                InetAddress host = serviceInfo.getHost();
                sendMealMessage(host, port);

            }
        };
    }

    private void sendMealMessage(InetAddress host, int port){
        Log.d(TAG, "sendMealMessage.host: " + host);
        Log.d(TAG, "sendMealMessage.port: " + port);
        Intent sendMealIntent = new Intent(this, mealCommandSenderService.class);
        Log.d(TAG, "sendMealMessage.after creating sendMealIntent");
        Bundle extras = new Bundle();
        extras.putSerializable("HOST", host);
        extras.putInt("PORT", port);
        extras.putString("MEAL", meal);
        sendMealIntent.putExtra("EXTRA_RECEIVER", extras);
        Log.d(TAG, "MainActivity.sendMealMessage.meal: " + meal);
        Log.d(TAG, "sendMealMessage.sendMealIntent: " + sendMealIntent.toString());
        startService(sendMealIntent);
    }

    private void showToast(String message){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
