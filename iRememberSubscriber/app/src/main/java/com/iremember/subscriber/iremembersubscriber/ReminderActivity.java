package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class ReminderActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        setBackgroundColor();
        setTextColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String reminderText = getTextFromIntent();
        displayText(reminderText);
        startMediaPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMediaPlayer();
    }

    public void onStopClick(View view) {
        finish();
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
        return (intent != null) ? intent.getStringExtra("command") : "";
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
}
