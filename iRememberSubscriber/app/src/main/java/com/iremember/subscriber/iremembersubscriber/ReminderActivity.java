package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ReminderActivity extends AppCompatActivity {
    private TextView tvReminderLabel;
    private MediaPlayer mMusicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        log("ReminderActivity.onCreate()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("ReminderActivity.onStart()");
        tvReminderLabel = (TextView) findViewById(R.id.tv_reminder_label);
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("ReminderActivity.onResume()");
        Intent remindIntent = getIntent();
        String mString = remindIntent.getStringExtra("meal_command");
        log("ReminderActivity.onResume; " + mString);
        tvReminderLabel.setText(mString);

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
        log("ReminderActivity.onStop");
        mMusicPlayer.release();
        mMusicPlayer = null;
    }

    public void onStopClick(View view) {
        finish();
    }
}
