package com.iremember.subscriber.iremembersubscriber;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnStart;
    private Button btnStop;
    private EditText editTextReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        editTextReceived = (EditText) findViewById(R.id.editTextReceived);
    }

    public void btnStartOnClick(View v){
        editTextReceived.setText("Start");
        Intent commandReceiverIntent = new Intent(this, CommandListenerService.class);
        commandReceiverIntent.putExtra(Intent.EXTRA_TEXT, "Hello from commandreceiver service"
        );
        commandReceiverIntent.setType("text/plain");
        Log.d(TAG, "Step1" );
        startService(commandReceiverIntent);
        Log.d(TAG, "Step3");
    }

    public void btnStopOnClick(View v){
        editTextReceived.setText("Stop");
    }
}
