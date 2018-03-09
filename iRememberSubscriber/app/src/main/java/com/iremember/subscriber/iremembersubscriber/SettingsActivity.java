package com.iremember.subscriber.iremembersubscriber;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onInfoClick(View view) {
        showUserMessage("Info");
    }

    public void onBgColorClick(View view) {
        showUserMessage("Background Color");
    }

    public void onTextColorClick(View view) {
        showUserMessage("Text Color");
    }

    public void onMusicClick(View view) {
        showUserMessage("Music");
    }
    
    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
    }
}
