package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View v) {
        //Hämta texten från textvyn
        String roomID = (String) ((TextView) findViewById(R.id.tv_roomid)).getText().toString();
        // om tom
        if((roomID != null) && !roomID.trim().equals("")){
            // - registrera tjänsten
            makeAvailable(roomID);

            //      om det gick att registrera
            //          - visa stb vy
            //          - spara namnet (rummsid) på enheten
            //          - starta tjänsten (UDP)
            //      annars...felmeddelande to be defined

        }else{
            // Visa toast
        }


    }

    /**
     * Start the service that performs service registration and UDP-service
     */
    private void makeAvailable(String roomID){
        Intent mIntent = new Intent(this, AvailabilityService.class);
        mIntent.putExtra("roomId", roomID);
        startService(mIntent);
    }
}
