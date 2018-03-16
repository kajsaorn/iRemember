package com.iremember.subscriber.iremembersubscriber;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isServiceRunning(NetworkService.class)) {
            connectToNetwork();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new ConnectionMessageReceiver();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * Called when settings button is clicked.
     */
    public void onSettingsClick(View view) {
        showSettingsActivity();
    }

    /**
     * Called when disconnect button is clicked.
     */
    public void onDisconnectClick(View v) {
        disconnectFromNetwork();
    }

    /**
     * Make this device discoverable on local network.
     */
    private void connectToNetwork() {
        log("connectToNetwork()");
        Intent intent = new Intent(this, NetworkService.class);
//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            log("before startService");
            startService(intent);
        }
    }

    /**
     * Stop the service handling network connection and device discoverability.
     */
    private void disconnectFromNetwork() {
        Intent intent = new Intent(this, NetworkService.class);
        stopService(intent);
    }

    /**
     * Check if an Android service is running.
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show start activity.
     */
    private void showStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
    }

    /**
     * Show settings activity.
     */
    private void showSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void log(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", message);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * BroadcastReceiver class that enables services to broadcastAction messages to this activity.
     */
    private class ConnectionMessageReceiver extends BroadcastReceiver {

        public ConnectionMessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.DISCONNECTION_SUCCESS);
            intentFilter.addAction(Broadcast.DISCONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.SOCKET_FAILURE);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.DISCONNECTION_SUCCESS:
                    showUserMessage(UserMessage.DISCONNECTION_SUCCESS);
                    showStartActivity();
                    finish();
                    break;
                case Broadcast.DISCONNECTION_FAILURE:
                    showUserMessage(Broadcast.DISCONNECTION_FAILURE);
                    break;
                case Broadcast.SOCKET_FAILURE:
                    showUserMessage(UserMessage.SOCKET_FAILURE);
                    showStartActivity();
                    finish();
                    break;
            }
        }
    }
}
