package com.iremember.subscriber.iremembersubscriber;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsBackgroundFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsInfoFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsMusicFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsTextFragment;

public class SettingsActivity extends AppCompatActivity {

    private int mCurrentSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCurrentSetting = (savedInstanceState != null) ? savedInstanceState.getInt("mCurrentSetting") : R.id.btn_info;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mCurrentSetting", mCurrentSetting);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when info tab is clicked.
     */
    public void onInfoClick(View view) {
        mCurrentSetting = R.id.btn_info;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when background tab is clicked.
     */
    public void onBgColorClick(View view) {
        mCurrentSetting = R.id.btn_bg_color;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when text tab is clicked.
     */
    public void onTextColorClick(View view) {
        mCurrentSetting = R.id.btn_txt_color;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when music tab is clicked.
     */
    public void onMusicClick(View view) {
        mCurrentSetting = R.id.btn_music;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Display a settings fragment to user and replace old one if there is one.
     */
    private void showCurrentSettingFragment() {
        Fragment fragment;

        switch (mCurrentSetting) {
            case R.id.btn_info:
                fragment = new SettingsInfoFragment();
                break;
            case R.id.btn_bg_color:
                fragment = new SettingsBackgroundFragment();
                break;
            case R.id.btn_txt_color:
                fragment = new SettingsTextFragment();
                break;
            case R.id.btn_music:
                fragment = new SettingsMusicFragment();
                break;
            default:
                return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Display a certain tab as selected to user.
     */
    private void markCurrentSettingTab() {
        findViewById(R.id.btn_info).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.btn_bg_color).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.btn_txt_color).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.btn_music).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(mCurrentSetting).setBackgroundResource(R.drawable.tab_selected);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Debug.
     */
    public void log(String msg) {
        Log.d("SettingsActivity", msg);
    }
}
