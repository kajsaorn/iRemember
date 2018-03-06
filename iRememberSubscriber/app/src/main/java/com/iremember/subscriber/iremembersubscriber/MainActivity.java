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

import com.iremember.subscriber.iremembersubscriber.Constants.NetworkActions;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private Button btnConnect, btnDisconnect;
    private TextView tvStatus;
    private EditText etRoomName;
    private String mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBroadcastReceiver = new MessageReceiver();
        initializeGUIElements();
        showDisconnectedMode();
        fetchRoomName();
    }

    private void initializeGUIElements() {
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        etRoomName = (EditText) findViewById(R.id.et_room_name);
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
        connectToNetwork(mRoomName);
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
        //...
        Intent disconnectIntent = new Intent(this, NetworkService.class);
        stopService(disconnectIntent);
    }

    private void showConnectedMode() {
        tvStatus.setText(R.string.status_connected);
        btnConnect.setVisibility(View.INVISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
        etRoomName.setVisibility(View.INVISIBLE);
    }

    private void showDisconnectedMode() {
        tvStatus.setText(R.string.status_disconnected);
        btnConnect.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.INVISIBLE);
        if (mRoomName == null) {
            etRoomName.setVisibility(View.VISIBLE);
        }
    }

    public void log(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NetworkActions.CONNECTION_SUCCESS);
            intentFilter.addAction(NetworkActions.CONNECTION_FAILURE);
            intentFilter.addAction(NetworkActions.DISCONNECTION_SUCCESS);
            intentFilter.addAction(NetworkActions.DISCONNECT_FAILURE);
            intentFilter.addAction(NetworkActions.SOCKET_FAILURE);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case NetworkActions.CONNECTION_SUCCESS:
                    showConnectedMode();
                    break;
                case NetworkActions.DISCONNECTION_SUCCESS:
                    showDisconnectedMode();
                    break;
                case NetworkActions.CONNECTION_FAILURE:
                    // ...
                    break;
                case NetworkActions.DISCONNECT_FAILURE:
                    // ...
                    break;
                case NetworkActions.SOCKET_FAILURE:
                    // ...
                    break;
            }
        }
    }

}
