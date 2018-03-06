package com.iremember.master.iremembermaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.iremember.master.iremembermaster.Constants.Commands;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBreakfastClick(View view) {
        log(this.getApplicationContext().toString());
        NetworkHandler mNetworkHandler = new NetworkHandler(Commands.BREAKFAST,
                this.getApplicationContext());
    }

    public void onLunchClick(View view) {
    }

    public void onDinnerClick(View view) {
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
