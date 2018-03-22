package com.iremember.subscriber.iremembersubscriber;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsBackgroundFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsGeneralFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsInfoFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsMusicFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsScreensaverFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsTextFragment;

public class SettingsActivity extends AppCompatActivity {

    private int mCurrentSetting;
    private Fragment mInfoSettingsFragment;
    private Fragment mBackgroundSettingsFragment;
    private Fragment mTextSettingsFragment;
    private Fragment mMusicSettingsFragment;
    private Fragment mGeneralSettingsFragment;
    private Fragment mScreensaverSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mInfoSettingsFragment = new SettingsInfoFragment();
        mBackgroundSettingsFragment = new SettingsBackgroundFragment();
        mTextSettingsFragment = new SettingsTextFragment();
        mMusicSettingsFragment = new SettingsMusicFragment();
        mGeneralSettingsFragment = new SettingsGeneralFragment();
        mScreensaverSettingsFragment = new SettingsScreensaverFragment();

        mCurrentSetting = (savedInstanceState != null) ? savedInstanceState.getInt("mCurrentSetting") : R.id.tab_info;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mCurrentSetting", mCurrentSetting);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when a tab is clicked.
     */
    public void onTabClick(View view) {
        mCurrentSetting = view.getId();
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Display a settings fragment to user and replace old one if there is one.
     */
    private void showCurrentSettingFragment() {
        Fragment fragment;

        switch (mCurrentSetting) {
            case R.id.tab_info:
                fragment = mInfoSettingsFragment;
                break;
            case R.id.tab_background:
                fragment = mBackgroundSettingsFragment;
                break;
            case R.id.tab_text:
                fragment = mTextSettingsFragment;
                break;
            case R.id.tab_music:
                fragment = mMusicSettingsFragment;
                break;
            case R.id.tab_screensaver:
                fragment = mScreensaverSettingsFragment;
                break;
            case R.id.tab_general:
                fragment = mGeneralSettingsFragment;
                break;
            default:
                return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_settings, fragment);
        transaction.commit();
    }

    /**
     * Display a certain tab as selected to user.
     */
    private void markCurrentSettingTab() {
        LinearLayout tabContainer = findViewById(R.id.tab_container);

        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            tabContainer.getChildAt(i).setBackgroundResource(R.drawable.tab_unselected);
        }
        findViewById(mCurrentSetting).setBackgroundResource(R.drawable.tab_selected);
    }
}
