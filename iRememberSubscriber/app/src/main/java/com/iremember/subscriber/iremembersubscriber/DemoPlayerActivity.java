package com.iremember.subscriber.iremembersubscriber;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class DemoPlayerActivity extends AppCompatActivity {
    private Button btnBreakfast;
    private boolean playingBreakfast = false;
    private Button btnLunch;
    private boolean playingLunch = false;
    private Button btnDinner;
    private boolean playingDinner = false;
    private MediaPlayer mealEventPlayer;

    private static final String TAG = "DemoPlayerActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_player);
        btnBreakfast = (Button) findViewById(R.id.btnBreakfast);
    }

    public void btnBreakfastOnClick(View v){
        Log.d(TAG, "btnBreakfastOnClick" );
        if(playingBreakfast) {
            btnBreakfast.setText("PLAY BREAKFAST");
            mealEventPlayer.release();
            mealEventPlayer = null;
            playingBreakfast = false;
        }else{
            Log.d(TAG, "btnBreakfastOnClick2" );
            btnBreakfast.setText("STOP BREAKFAST");
            mealEventPlayer = MediaPlayer.create(this,R.raw.cosifantutte);
            Log.d(TAG, "btnBreakfastOnClick3" );
            mealEventPlayer.start();
            Log.d(TAG, "btnBreakfastOnClick4" );
            playingBreakfast = true;
        }

    }
}
