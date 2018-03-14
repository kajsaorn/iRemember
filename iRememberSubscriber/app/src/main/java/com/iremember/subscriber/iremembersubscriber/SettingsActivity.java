package com.iremember.subscriber.iremembersubscriber;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsBackgroundFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsGeneralFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsInfoFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsMusicFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.SettingsTextFragment;

public class SettingsActivity extends AppCompatActivity {

    private int mCurrentSetting;
    private Fragment mInfoSettingsFragment;
    private Fragment mBackgroundSettingsFragment;
    private Fragment mTextSettingsFragment;
    private Fragment mMusicSettingsFragment;
    private Fragment mGeneralSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mInfoSettingsFragment = new SettingsInfoFragment();
        mBackgroundSettingsFragment = new SettingsBackgroundFragment();
        mTextSettingsFragment = new SettingsTextFragment();
        mMusicSettingsFragment = new SettingsMusicFragment();
        mGeneralSettingsFragment = new SettingsGeneralFragment();

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
     * Called when info tab is clicked.
     */
    public void onInfoClick(View view) {
        mCurrentSetting = R.id.tab_info;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when general tab is clicked.
     */
    public void onGeneralClick(View view) {
        mCurrentSetting = R.id.tab_general;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when background tab is clicked.
     */
    public void onBgColorClick(View view) {
        mCurrentSetting = R.id.tab_background;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when text tab is clicked.
     */
    public void onTextColorClick(View view) {
        mCurrentSetting = R.id.tab_text;
        showCurrentSettingFragment();
        markCurrentSettingTab();
    }

    /**
     * Called when music tab is clicked.
     */
    public void onMusicClick(View view) {
        mCurrentSetting = R.id.tab_music;
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
            case R.id.tab_general:
                fragment = mGeneralSettingsFragment;
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
        findViewById(R.id.tab_info).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_general).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_background).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_text).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_music).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(mCurrentSetting).setBackgroundResource(R.drawable.tab_selected);
    }
}
