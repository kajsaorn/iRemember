package com.iremember.subscriber.iremembersubscriber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Network;

public class MainActivity extends AppCompatActivity {

    private String mRoomName;
    private TextView mTvStatus;
    private EditText mEtRoomName;
    private Button mBtnConnect, mBtnDisconnect;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGUIElements();
        showDisconnectedMode();
        fetchRoomName();
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

    private void initializeGUIElements() {
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mEtRoomName = (EditText) findViewById(R.id.et_room_name);
    }

    private void fetchRoomName() {
        // Read room name from persistent memory.
        // If there is no previous room name, set null.
        mRoomName = null;
    }

    /**
     * Called when connect button is clicked.
     */
    public void onConnectClick(View v) {
        NotificationUtils utils = new NotificationUtils();
        utils.createNotification("Test Title", "Test text", this);

/*
        if (mRoomName == null) {
            String input = ((EditText) findViewById(R.id.et_room_name)).getText().toString();

            if (input != null && !input.trim().equals("")) {
                mRoomName = input;
                // Save room name to persistent(?) memory.
            } else {
                log("Ogiltigt rumsnummer");
                return;
            }
        }
        connectToNetwork(mRoomName);*/
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
    private void connectToNetwork(String roomName) {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("roomName", roomName);
        startService(intent);
    }

    private void disconnectFromNetwork() {
        Intent intent = new Intent(this, NetworkService.class);
        stopService(intent);
    }

    private void showConnectedMode() {
        mTvStatus.setText(R.string.status_connected);
        mBtnConnect.setVisibility(View.INVISIBLE);
        mBtnDisconnect.setVisibility(View.VISIBLE);
        mEtRoomName.setVisibility(View.INVISIBLE);
    }

    private void showDisconnectedMode() {
        mTvStatus.setText(R.string.status_disconnected);
        mBtnConnect.setVisibility(View.VISIBLE);
        mBtnDisconnect.setVisibility(View.INVISIBLE);
        if (mRoomName == null) {
            mEtRoomName.setVisibility(View.VISIBLE);
        }
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
                    showConnectedMode();
                    break;
                case Network.DISCONNECTION_SUCCESS:
                    showDisconnectedMode();
                    break;
                case Network.CONNECTION_FAILURE:
                    // ...
                    break;
                case Network.DISCONNECTION_FAILURE:
                    // ...
                    break;
                case Network.SOCKET_FAILURE:
                    // ...
                    break;
            }
        }
    }
}
