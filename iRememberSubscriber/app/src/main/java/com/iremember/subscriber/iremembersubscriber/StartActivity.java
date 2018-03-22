package com.iremember.subscriber.iremembersubscriber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class StartActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).edit().clear().commit(); // TODO: Remove this later
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        stopNetworkService();
        setDozeMode();
    }

    @Override
    protected void onStop() {
        unregisterBroadcastReceiver();
        super.onStop();
    }

    /**
     * Called when user clicks start button. If user has previously provided a
     * room name and an iRemember Master Service name, the network service is started.
     */
    public void onStartClick(View view) {
        String mRoomName = PreferenceUtils.readRoomName(this);
        String mServiceName = PreferenceUtils.readMasterServiceName(this);

        if (mRoomName == null && mServiceName == null) {
            showUserMessage(UserMessage.MISSING_ROOM_AND_SERVICE_NAME);
        } else if (mRoomName == null) {
            showUserMessage(UserMessage.MISSING_ROOM_NAME);
        } else if (mServiceName == null) {
            showUserMessage(UserMessage.MISSING_SERVICE_NAME);
        } else {
            startNetworkService();
        }
    }

    /**
     * Register broadcast receiver so that this activity listens
     * to broadcast messages from other activities or services.
     */
    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new StartActivity.StartupMessageReceivier();
        }
    }

    /**
     * Unregister broadcast receiver so that this activity stop
     * listening to broadcast messages from other activities or services.
     */
    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * Ask user for permission to use unoptimized battery settings.
     */
    private void setDozeMode() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    /**
     * Start network service which, among other things, will try to connect to a
     * previously defined remote iRemember Master Service. If the connection succeeds
     * this activity will receive a broadcast message and the main activity is shown,
     * otherwise an error message.
     */
    private void startNetworkService() {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra(Broadcast.SEARCH_MASTER_SERVICE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * Stop network service.
     */
    private void stopNetworkService() {
        Intent intent = new Intent(this, NetworkService.class);
        stopService(intent);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Start main activity.
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Called when discovery button is clicked. Starts the settings activity, where
     * user may define a room name and discover available iRemember Master Services.
     */
    public void onDiscoveryClick(View view) {
        Intent intent = new Intent(this, DiscoveryActivity.class);
        startActivity(intent);
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class StartupMessageReceivier extends BroadcastReceiver {

        public StartupMessageReceivier() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.MISSING_ROOM_NAME);
            intentFilter.addAction(Broadcast.MISSING_SERVICE_NAME);
            intentFilter.addAction(Broadcast.DISCOVERY_FAILURE);
            intentFilter.addAction(Broadcast.CONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.CONNECTION_SUCCESS);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.MISSING_ROOM_NAME:
                    showUserMessage(UserMessage.MISSING_ROOM_NAME);
                    stopNetworkService();
                    break;
                case Broadcast.MISSING_SERVICE_NAME:
                    showUserMessage(UserMessage.MISSING_SERVICE_NAME);
                    stopNetworkService();
                    break;
                case Broadcast.DISCOVERY_FAILURE:
                    showUserMessage(UserMessage.DISCOVERY_FAILURE);
                    stopNetworkService();
                    break;
                case Broadcast.CONNECTION_FAILURE:
                    showUserMessage(UserMessage.CONNECTION_FAILURE);
                    stopNetworkService();
                    break;
                case Broadcast.CONNECTION_SUCCESS:
                    startMainActivity();
                    finish();
                    break;
            }
        }
    }
}
