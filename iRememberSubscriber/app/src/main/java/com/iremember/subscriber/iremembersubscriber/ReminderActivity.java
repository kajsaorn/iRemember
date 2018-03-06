package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReminderActivity extends AppCompatActivity {

    private TextView mTvReminderLabel;
    private MediaPlayer mMusicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        mTvReminderLabel = (TextView) findViewById(R.id.tv_reminder_label);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String command = intent.getStringExtra("command");
        mTvReminderLabel.setText(command);
        mMusicPlayer = MediaPlayer.create(this, R.raw.cosifantutte);
        mMusicPlayer.start();
    }

    public void log(String msg) {
        Log.d("ReminderActivity", msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMusicPlayer.release();
        mMusicPlayer = null;
    }

    public void onStopClick(View view) {
        finish();
    }
}
