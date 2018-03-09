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

    private final int TAB_INFO = R.id.btn_info;
    private final int TAB_BACKGROUND = R.id.btn_bg_color;
    private final int TAB_TEXT = R.id.btn_txt_color;
    private final int TAB_MUSIC = R.id.btn_music;
    private int mCurrentTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        showFragment(new SettingsInfoFragment());
        selectTab(TAB_INFO);
    }

    public void onInfoClick(View view) {
        showFragment(new SettingsInfoFragment());
        selectTab(TAB_INFO);
    }

    public void onBgColorClick(View view) {
        showFragment(new SettingsBackgroundFragment());
        selectTab(TAB_BACKGROUND);
    }

    public void onTextColorClick(View view) {
        showFragment(new SettingsTextFragment());
        selectTab(TAB_TEXT);
    }

    public void onMusicClick(View view) {
        showFragment(new SettingsMusicFragment());
        selectTab(TAB_MUSIC);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void selectTab(int tab) {
        if (mCurrentTab > 0) {
            findViewById(mCurrentTab).setBackgroundResource(R.drawable.tab_unselected);
        }
        findViewById(tab).setBackgroundResource(R.drawable.tab_selected);
        mCurrentTab = tab;
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
