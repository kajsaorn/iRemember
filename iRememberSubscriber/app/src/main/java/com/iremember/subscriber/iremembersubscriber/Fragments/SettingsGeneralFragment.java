package com.iremember.subscriber.iremembersubscriber.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class SettingsGeneralFragment extends Fragment {

    private View mFragment;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_settings_general, container, false);
        mContext = mFragment.getContext();
        initComponents();
        displayCurrentSettings();
        return mFragment;
    }

    private void initComponents() {
        View.OnClickListener radioButtonListener = new RadioButtonListener();
        mFragment.findViewById(R.id.rb_allow_reminders_yes).setOnClickListener(radioButtonListener);
        mFragment.findViewById(R.id.rb_allow_reminders_no).setOnClickListener(radioButtonListener);
        mFragment.findViewById(R.id.rb_allow_music_yes).setOnClickListener(radioButtonListener);
        mFragment.findViewById(R.id.rb_allow_music_no).setOnClickListener(radioButtonListener);
    }

    private void displayCurrentSettings() {
        boolean remindersAllowed = PreferenceUtils.readRemindersAllowed(mContext);
        ((RadioButton) mFragment.findViewById(R.id.rb_allow_reminders_yes)).setChecked(remindersAllowed);
        ((RadioButton) mFragment.findViewById(R.id.rb_allow_reminders_no)).setChecked(!remindersAllowed);

        boolean musicAllowed = PreferenceUtils.readMusicAllowed(mContext);
        ((RadioButton) mFragment.findViewById(R.id.rb_allow_music_yes)).setChecked(musicAllowed);
        ((RadioButton) mFragment.findViewById(R.id.rb_allow_music_no)).setChecked(!musicAllowed);
    }

    private class RadioButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int btnId = view.getId();

            switch (btnId) {
                case R.id.rb_allow_reminders_yes:
                    PreferenceUtils.writeAllowReminders(mContext, true);
                    PreferenceUtils.showUserConfirmation(mContext);
                    break;
                case R.id.rb_allow_reminders_no:
                    PreferenceUtils.writeAllowReminders(mContext, false);
                    PreferenceUtils.showUserConfirmation(mContext);
                    break;
                case R.id.rb_allow_music_yes:
                    PreferenceUtils.writeAllowMusic(mContext, true);
                    PreferenceUtils.showUserConfirmation(mContext);
                    break;
                case R.id.rb_allow_music_no:
                    PreferenceUtils.writeAllowMusic(mContext, false);
                    PreferenceUtils.showUserConfirmation(mContext);
                    break;
            }
        }
    }
}
