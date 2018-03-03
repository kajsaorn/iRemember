package com.iremember.subscriber.iremembersubscriber;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.iremember.subscriber.iremembersubscriber.Utilities;

import static com.iremember.subscriber.iremembersubscriber.Utilities.createNotification;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnStart;
    private Button btnStop;
    private Button btnToPlay;
    private Button btnRegisterNSD;
    private EditText editTextReceived;
    private NsdManager.RegistrationListener mRegistrationListener;
    private String mServiceName;
    private NsdManager mNsdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        editTextReceived = (EditText) findViewById(R.id.editTextReceived);
        btnRegisterNSD = (Button)findViewById(R.id.btnStartNSD);
    }

    public void btnStartOnClick(View v){
        editTextReceived.setText("Start");
        Intent commandReceiverIntent = new Intent(this, CommandListenerService.class);
        commandReceiverIntent.putExtra(Intent.EXTRA_TEXT, "Hello from commandreceiver service"
        );
        commandReceiverIntent.setType("text/plain");
         startService(commandReceiverIntent);
    }

    public void btnStopOnClick(View v){
        editTextReceived.setText("Stop");
    }

    public void btnToPlayOnClick(View v){
        Intent startDemoPlayerIntent = new Intent(this, DemoPlayerActivity.class);
        startActivity(startDemoPlayerIntent);
    }

    public void btnRegisterNSDOnClick(View v){
        Log.d(TAG, "btnRegisterNSDOnClick");
        initializeRegistrationListener();
        registerService();
    }

    public void registerService() {
        Log.d(TAG, "registerService");
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("room1");
        serviceInfo.setServiceType("_iremember._udp");
        serviceInfo.setPort(8888);
        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        Log.d(TAG, "initializeRegistrationListener");
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                createNotification("Service registered", "registered", "iremember", 2, getApplicationContext());
                Log.d(TAG, "Service registered" );
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                Log.d(TAG, "Registration failed, " + serviceInfo.toString() + " , " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }
}
