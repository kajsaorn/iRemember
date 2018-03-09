package com.iremember.subscriber.iremembersubscriber;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Network;
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
            mBroadcastReceiver = new MessageReceiver();
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
        Intent intent = new Intent(this, NetworkService.class);
        startService(intent);
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
    private void showStartActivity(String message) {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra(Network.MESSAGE, message);
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
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Network.CONNECTION_SUCCESS);
            intentFilter.addAction(Network.CONNECTION_FAILURE);
            intentFilter.addAction(Network.DISCONNECTION_SUCCESS);
            intentFilter.addAction(Network.DISCONNECTION_FAILURE);
            intentFilter.addAction(Network.SOCKET_FAILURE);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Network.CONNECTION_SUCCESS:
                    showUserMessage(getString(R.string.toast_connection_success));
                    break;
                case Network.DISCONNECTION_SUCCESS:
                    showStartActivity(Network.DISCONNECTION_SUCCESS);
                    finish();
                    break;
                case Network.CONNECTION_FAILURE:
                    showStartActivity(Network.DISCONNECTION_FAILURE);
                    finish();
                    break;
                case Network.DISCONNECTION_FAILURE:
                    showUserMessage(getString(R.string.toast_disconnection_failure));
                    break;
                case Network.SOCKET_FAILURE:
                    showStartActivity(Network.SOCKET_FAILURE);
                    finish();
                    break;
            }
        }
    }
}
