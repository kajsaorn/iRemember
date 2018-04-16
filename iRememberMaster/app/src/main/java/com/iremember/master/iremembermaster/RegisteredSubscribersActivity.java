package com.iremember.master.iremembermaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

import java.util.Map;

public class RegisteredSubscribersActivity extends AppCompatActivity {
    TextView tv_display_subscribers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_subscribers);
        tv_display_subscribers = (TextView) findViewById(R.id.tv_registered_subscribers);
        displaySubscribers();
    }

    public void onGoToMenuClick(View view) {
        finish();
    }

    public void onUppdateClick(View v) {
        displaySubscribers();
    }

    private void displaySubscribers() {
       Map<String, ?> subscribers = PreferenceUtils.getAllSubscribers(this);
       String strSubscribers = "\n" + getString(R.string.txt_registered_rooms) + "\n\n";
        for (String roomName : subscribers.keySet()) {
            strSubscribers = strSubscribers + roomName + "\n";
        }
        tv_display_subscribers.setText(strSubscribers);
    }
}
