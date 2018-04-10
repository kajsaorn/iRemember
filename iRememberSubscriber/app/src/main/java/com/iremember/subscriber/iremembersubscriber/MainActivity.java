package com.iremember.subscriber.iremembersubscriber;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isServiceRunning(NetworkService.class) ) {
            showStartActivity();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        showCurrentUserRestrictions();
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }

    /**
     * Called when settings button is clicked. Starts settings activity.
     */
    public void onSettingsClick(View view) {
        showSettingsActivity();
    }

    /**
     * Called when disconnect button is clicked. Stops the network service.
     */
    public void onDisconnectClick(View v) {
        stopNetworkService();
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new ConnectionMessageReceiver();
        }
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * Stop network service, which will try to disconnect from current
     * remote iRemember Master Service. If the disconnection succeeds
     * the start activity is shown, otherwise an error message.
     */
    private void stopNetworkService() {
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
     * Show current user restrictions, e.g. that music is not allowed.
     */
    private void showCurrentUserRestrictions() {
        boolean isMusicAllowed = PreferenceUtils.readMusicAllowed(this);
        int visibility = (isMusicAllowed) ? View.GONE : View.VISIBLE;
        findViewById(R.id.tv_restriction_label).setVisibility(visibility);
        findViewById(R.id.tv_restriction_music).setVisibility(visibility);
    }

    /**
     * Show wifi error message. This should be called when wifi state
     * changes and the new wifi is not the one where the app previously
     * connected to the iRemember Master Service.
     */
    private void showWifiErrorMessage() {
        findViewById(R.id.tv_wrong_wifi).setVisibility(View.VISIBLE);
    }

    /**
     * Hide wifi error message. This should be called when as soon
     * as the app is connected to the iRemember Master Service.
     */
    private void hideWifiErrorMessage() {
        findViewById(R.id.tv_wrong_wifi).setVisibility(View.GONE);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class ConnectionMessageReceiver extends BroadcastReceiver {

        public ConnectionMessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.CONNECTION_SUCCESS);
            intentFilter.addAction(Broadcast.NETWORK_SERVICE_OFF);
            intentFilter.addAction(Broadcast.DISCONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.SOCKET_FAILURE);
            intentFilter.addAction(Broadcast.DISCONNECTED_WIFI);
            intentFilter.addAction(Broadcast.RECONNECTED_WIFI);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.CONNECTION_SUCCESS:
                    hideWifiErrorMessage();
                    break;
                case Broadcast.CONNECTION_SOCKET_FAILURE:
                    showUserMessage(UserMessage.CONNECTION_FAILURE);
                    break;
                case Broadcast.DISCONNECTION_FAILURE:
                    showUserMessage(UserMessage.DISCONNECTION_FAILURE);
                    break;
                case Broadcast.SOCKET_FAILURE:
                    showUserMessage(UserMessage.SOCKET_FAILURE);
                    showStartActivity();
                    finish();
                    break;
                case Broadcast.NETWORK_SERVICE_OFF:
                    //showUserMessage(UserMessage.NETWORK_SERVICE_OFF);
                    //showStartActivity();
                    //finish();
                    finish();
                    System.exit(0);
                    break;
                case Broadcast.DISCONNECTED_WIFI:
                    showWifiErrorMessage();
                    break;
                case Broadcast.RECONNECTED_WIFI:
                    hideWifiErrorMessage();
                    break;

            }
        }
    }
}
