package com.iremember.master.iremembermaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBreakfastClick(View view) {
        new CommandHandler(Command.BREAKFAST, this);
    }

    public void onLunchClick(View view) {
        new CommandHandler(Command.LUNCH, this);
    }

    public void onDinnerClick(View view) {
        new CommandHandler(Command.DINNER, this);
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}