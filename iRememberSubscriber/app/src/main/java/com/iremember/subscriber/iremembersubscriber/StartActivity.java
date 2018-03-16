package com.iremember.subscriber.iremembersubscriber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class StartActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSharedPreferences(BuildConfig.APPLICATION_ID, 0).edit().clear().commit();    // Remove later
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new StartActivity.StartupMessageReceivier();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * Called when user clicks start button.
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
            Intent intent = new Intent(this, NetworkService.class);
            intent.putExtra(Broadcast.SERVICE_NAME, mServiceName);
            intent.putExtra(Broadcast.ROOM_NAME, mRoomName);
            startService(intent);
        }
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Start main activity (where user will be connected to network and discoverable).
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view) {
        Log.d("Settings", "Clicked Button");
        Intent intent = new Intent(this, DiscoveryActivity.class);
        startActivity(intent);
    }

    /**
     * BroadcastReceiver class that enables services to broadcastAction messages to this activity.
     */
    private class StartupMessageReceivier extends BroadcastReceiver {

        public StartupMessageReceivier() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.DISCOVERY_FAILURE);
            intentFilter.addAction(Broadcast.CONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.MISSING_ROOM_NAME);
            intentFilter.addAction(Broadcast.MISSING_SERVICE_NAME);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.CONNECTION_FAILURE:
                    showUserMessage(UserMessage.CONNECTION_FAILURE);
                    break;
            }
        }
    }
}
