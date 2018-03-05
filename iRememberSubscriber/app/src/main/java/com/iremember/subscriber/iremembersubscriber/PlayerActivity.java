package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class PlayerActivity extends AppCompatActivity {
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mEditText = findViewById(R.id.mealText);
        Intent playIntent = getIntent();
        String mString = playIntent.getStringExtra("meal_command");
        mEditText.setText(mEditText.getText());

    }
}
