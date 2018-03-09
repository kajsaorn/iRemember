package com.iremember.subscriber.iremembersubscriber;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;

public class MainActivity extends AppCompatActivity {

    private TextView mTvStatus;
    private Button mBtnConnect, mBtnDisconnect;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSharedPreferences(BuildConfig.APPLICATION_ID, 0).edit().clear().commit();    // Remove later

        if (!isServiceRunning(NetworkService.class)) {
            showStartActivity();
        }
        //initializeGUIElements();
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

    private String fetchRoomName() {
        SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.ROOM_NAME, "");
    }

    private void showStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra(SharedPrefs.ROOM_NAME, fetchRoomName());
        startActivity(intent);
    }


    private void initializeGUIElements() {
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
    }


    public void onSettingsClick(View view) {
        log("Settings click");
    }

    /**
     * Called when disconnect button is clicked.
     */
    public void onDisconnectClick(View v) {
        log("Disconnect click");
        disconnectFromNetwork();
    }

    /**
     * Make this device discoverable on local network.
     */
    private void connectToNetwork() {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra(SharedPrefs.ROOM_NAME, fetchRoomName());
        startService(intent);
    }

    private void disconnectFromNetwork() {
        Intent intent = new Intent(this, NetworkService.class);
        stopService(intent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.CONNECTION_SUCCESS);
            intentFilter.addAction(Broadcast.CONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.DISCONNECTION_SUCCESS);
            intentFilter.addAction(Broadcast.DISCONNECTION_FAILURE);
            intentFilter.addAction(Broadcast.SOCKET_FAILURE);
            intentFilter.addAction(Broadcast.DO_CONNECT);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.CONNECTION_SUCCESS:
                    // ...
                    break;
                case Broadcast.DISCONNECTION_SUCCESS:
                    showStartActivity();
                    break;
                case Broadcast.CONNECTION_FAILURE:
                    //showStartActivity(); but tell them the last connection try failed
                    break;
                case Broadcast.DISCONNECTION_FAILURE:
                    // ...
                    break;
                case Broadcast.SOCKET_FAILURE:
                    // ...
                    break;
                case Broadcast.DO_CONNECT:
                    connectToNetwork();
                    break;
            }
        }
    }
}
