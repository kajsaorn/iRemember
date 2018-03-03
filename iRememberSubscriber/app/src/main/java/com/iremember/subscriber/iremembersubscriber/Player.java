package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class Player extends AppCompatActivity {
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mEditText = findViewById(R.id.mealText);
        Intent playIntent = getIntent();
        String mString = playIntent.getStringExtra(Intent.EXTRA_TEXT);
        mEditText.setText(mEditText.getText());

    }

}
