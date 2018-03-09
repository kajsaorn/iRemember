package com.iremember.subscriber.iremembersubscriber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;
import com.iremember.subscriber.iremembersubscriber.Utils.BroadcastUtils;

public class StartActivity extends AppCompatActivity {

    private String mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        fetchIntentContent();
        initializeGUIElements();
    }

    private void fetchIntentContent() {
        Intent intent = getIntent();
        mRoomName = intent.getStringExtra(SharedPrefs.ROOM_NAME);
    }

    /**
     * Show room name input field only if user hasn't picked a name before.
     */
    private void initializeGUIElements() {
        EditText mEtRoomName = (EditText) findViewById(R.id.et_room_name);
        mEtRoomName.setVisibility((mRoomName.equals("")) ? View.VISIBLE : View.GONE);
    }

    /**
     * Save room name to persistent memory.
     */
    private void saveRoomName() {
        SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(SharedPrefs.ROOM_NAME, mRoomName);
        editor.commit();
    }

    /**
     *
     */
    public void onConnectClick(View view) {
        if (mRoomName.equals("")) {
            EditText mEtRoomName = (EditText) findViewById(R.id.et_room_name);
            mRoomName = mEtRoomName.getText().toString().trim();
        }
        if (mRoomName.equals("")) {
            Toast.makeText(this, "Du glömde fylla i ett namn för ditt rum.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveRoomName();
        BroadcastUtils.broadcast(Broadcast.DO_CONNECT, getApplicationContext());
        finish();
    }
}
