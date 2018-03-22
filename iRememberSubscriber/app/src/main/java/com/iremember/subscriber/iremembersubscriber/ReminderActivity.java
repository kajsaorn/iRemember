package com.iremember.subscriber.iremembersubscriber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class ReminderActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    PowerManager.WakeLock mWakeLock = null;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        setBackgroundColor();
        setTextColor();
        turnScreenOn();
    }

    private void turnScreenOn(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String reminderText = getTextFromIntent();
        boolean isMusicAllowed = PreferenceUtils.readMusicAllowed(this);

        if (isMusicAllowed) {
            startMediaPlayer();
        }
        displayText(reminderText);
        registerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        unregisterBroadcastReceiver();
        stopMediaPlayer();
        super.onStop();
    }

    public void onStopClick(View view) {
        finish();
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new ReminderActivity.MessageReceiver();
        }
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void setBackgroundColor() {
        int backgroundColor = PreferenceUtils.readBackgroundColor(this);
        findViewById(R.id.reminder_container).setBackgroundColor(backgroundColor);
    }

    private void setTextColor() {
        int textColor = PreferenceUtils.readTextColor(this);
        ((TextView) findViewById(R.id.tv_reminder_label)).setTextColor(textColor);
    }

    private String getTextFromIntent() {
        Intent intent = getIntent();
        return (intent != null) ? intent.getStringExtra(Broadcast.MESSAGE) : "";
    }

    private void displayText(String text) {
        ((TextView) findViewById(R.id.tv_reminder_label)).setText(text);
    }

    private void startMediaPlayer() {
        String songTitle = PreferenceUtils.readSongTitle(this);

        if (songTitle != null) {
            int source = getResources().getIdentifier(songTitle, "raw", getPackageName());
            mMediaPlayer = MediaPlayer.create(this, source);
            mMediaPlayer.start();
        }
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.FINISH_ACTIVITY);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Broadcast.FINISH_ACTIVITY:
                    finish();
                    break;
            }
        }
    }
}
