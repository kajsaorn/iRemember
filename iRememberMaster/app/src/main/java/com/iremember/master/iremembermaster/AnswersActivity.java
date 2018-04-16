package com.iremember.master.iremembermaster;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.iremember.master.iremembermaster.Constants.Command;

public class AnswersActivity extends AppCompatActivity {
    TextView tv_display_answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers);
        tv_display_answers = findViewById(R.id.tv_answers);
        displayResults();
    }

    public void onAnsweredClick(View view) {
        finish();
    }

    private void displayResults() {
        String displayString = "";
        Intent intent = getIntent();
        String[] answers = intent.getStringArrayExtra(Command.ANSWERS);
        String[] noAnswers = intent.getStringArrayExtra(Command.NO_ANSWERS);

        if (answers != null && answers.length > 0) {
            displayString = displayString + getString(R.string.txt_answer_message) + "\n";
            for (int i=0; i < answers.length; i++) {
                displayString = displayString + answers[i] + "\n";
            }
        }

        if (noAnswers != null && noAnswers.length > 0) {
            if (!displayString.equals("")) {
                 displayString = displayString + "\n";
            }
            displayString = displayString + getString(R.string.txt_no_answer_message) + "\n";
            for (int i=0; i < noAnswers.length; i++) {
                displayString = displayString + noAnswers[i] + "\n";
            }
        }
        tv_display_answers.setText(displayString);
    }
}
