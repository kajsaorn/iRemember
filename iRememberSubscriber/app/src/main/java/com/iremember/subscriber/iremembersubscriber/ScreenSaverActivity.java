package com.iremember.subscriber.iremembersubscriber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class ScreenSaverActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        setScreensaverImage();
        turnOnScreenSaver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        unregisterBroadcastReceiver();
        super.onStop();
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new ScreenSaverActivity.MessageReceiver();
        }
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void setScreensaverImage() {
        String imagePath = PreferenceUtils.readScreensaverPath(this);
        ImageView ivScreensaver = (ImageView) findViewById(R.id.img_screensaver);
        Bitmap mBitmap = BitmapFactory.decodeFile(imagePath);

        if (mBitmap != null) {
            ivScreensaver.setImageBitmap(mBitmap);
        } else {
            Drawable mBackground = ResourcesCompat.getDrawable(getResources(), R.drawable.screensaver, null);
            ivScreensaver.setBackground(mBackground);
        }
    }

    private void turnOnScreenSaver(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void turnOffScreenSaver(View view) {
        finish();
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this activity.
     */
    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.FINISH_SCREENSAVER);
            registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Broadcast.FINISH_SCREENSAVER:
                    finish();
                    break;
            }
        }
    }
}
