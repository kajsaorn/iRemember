package com.iremember.subscriber.iremembersubscriber;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iremember.subscriber.iremembersubscriber.Fragments.DiscoveryInfoFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.DiscoveryRoomFragment;
import com.iremember.subscriber.iremembersubscriber.Fragments.DiscoveryServiceFragment;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;

public class DiscoveryActivity extends AppCompatActivity implements
        DiscoveryInfoFragment.OnStartConfigurationListener,
        DiscoveryRoomFragment.OnRoomNameSavedListener,
        DiscoveryServiceFragment.OnServiceClickListener {

    private int mCurrentFragmentId;
    private Fragment mInfoDiscoveryFragment;
    private Fragment mRoomDiscoveryFragment;
    private Fragment mServiceDiscoveryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DiscoveryActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        initFragments();

        int fragmentId = savedInstanceState != null ? savedInstanceState.getInt("mCurrentFragmentId") : R.id.fragment_discovery_info;
        showFragment(fragmentId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mCurrentFragmentId", mCurrentFragmentId);
        super.onSaveInstanceState(outState);
    }

    private void initFragments() {
        mInfoDiscoveryFragment = new DiscoveryInfoFragment();
        mRoomDiscoveryFragment = new DiscoveryRoomFragment();
        mServiceDiscoveryFragment = new DiscoveryServiceFragment();
    }

    /**
     * Display a fragment to user and replace old one if there is one.
     */
    private void showFragment(int id) {
        mCurrentFragmentId = id;
        Fragment fragment;

        switch (mCurrentFragmentId) {
            case R.id.fragment_discovery_info:
                fragment = mInfoDiscoveryFragment;
                break;
            case R.id.fragment_discovery_room:
                fragment = mRoomDiscoveryFragment;
                break;
            case R.id.fragment_discovery_services:
                fragment = mServiceDiscoveryFragment;
                break;
            default:
                return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_discovery, fragment);
        transaction.commit();
    }

    @Override
    public void onStartConfiguration() {
        showFragment(R.id.fragment_discovery_room);
    }

    @Override
    public void onRoomNameSaved() {
        showFragment(R.id.fragment_discovery_services);
    }

    @Override
    public void onServiceSaved() {
        finish();
    }

    @Override
    public void onSearchServices() {
        mServiceDiscoveryFragment = new DiscoveryServiceFragment();
        showFragment(R.id.fragment_discovery_services);
    }

    @Override
    public void onBackClick() {
        finish();
    }
}
