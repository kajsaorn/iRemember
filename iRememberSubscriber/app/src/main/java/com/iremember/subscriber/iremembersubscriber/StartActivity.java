package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Network;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSharedPreferences(BuildConfig.APPLICATION_ID, 0).edit().clear().commit();    // Remove later
        initializeGUIElements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStartupInfo();
    }

    /**
     * Called when user clicks connect button.
     * If user input is valid, main activity will be started.
     */
    public void onConnectClick(View view) {
        String mRoomName = PreferenceUtils.readRoomName(this);

        if (mRoomName == null || mRoomName.equals("")) {
            EditText mEtRoomName = (EditText) findViewById(R.id.et_room_name);
            mRoomName = mEtRoomName.getText().toString().trim();

            if (mRoomName.equals("")) {
                showUserMessage(getString(R.string.toast_roomname_invalid));
                return;
            } else {
                PreferenceUtils.writeRoomName(this, mRoomName);
            }
        }
        startMainActivity();
        finish();
    }

    /**
     * Check if there is any start up info, e.g. if the previous connection failed.
     */
    private void checkStartupInfo() {
        String info = getIntent().getStringExtra(Network.MESSAGE);
        if (info != null) {
            switch (info) {
                case Network.CONNECTION_FAILURE:
                    showUserMessage(getString(R.string.toast_connection_failure));
                    break;
                case Network.DISCONNECTION_SUCCESS:
                    showUserMessage(getString(R.string.toast_disconnection_success));
                    break;
                case Network.SOCKET_FAILURE:
                    showUserMessage(getString(R.string.toast_socket_failure));
                    break;
            }
        }
    }

    /**
     * Show room name input field only if user hasn't picked a name before.
     */
    private void initializeGUIElements() {
        String mRoomName = PreferenceUtils.readRoomName(this);
        EditText mEtRoomName = (EditText) findViewById(R.id.et_room_name);
        mEtRoomName.setVisibility((mRoomName == null) ? View.VISIBLE : View.GONE);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Start main activity (where user will be connected to network and discoverable).
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
