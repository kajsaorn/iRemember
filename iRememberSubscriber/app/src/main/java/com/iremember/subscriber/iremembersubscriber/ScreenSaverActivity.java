package com.iremember.subscriber.iremembersubscriber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class ScreenSaverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        setScreensaverImage();
        turnOnScreenSaver();
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
}
