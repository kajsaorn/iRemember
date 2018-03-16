package com.iremember.subscriber.iremembersubscriber.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class DiscoveryServiceFragment extends Fragment {

    View mContent;
    OnServiceClickListener mListener;
    DiscoveryMessageReceiver mBroadcastReceiver;
    View tvSearching, tvNoServices, btnSearchServices, btnBack;
    LinearLayout layoutServices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("DiscoveryFragment", "onCreate()");
        mContent = inflater.inflate(R.layout.fragment_discovery_services, container, false);
        initComponents();
        initListeners();
        startNetworkService();
        return mContent;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onStop() {
        unregisterBroadcastReceiver();
        super.onStop();
    }

    public interface OnServiceClickListener {
        void onServiceSaved();
        void onSearchServices();
        void onBackClick();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (DiscoveryServiceFragment.OnServiceClickListener) getActivity();
    }

    private void initListeners() {
        btnSearchServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNetworkService();
                mListener.onSearchServices();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNetworkService();
                mListener.onBackClick();
            }
        });
    }

    private void initComponents() {
        tvSearching = mContent.findViewById(R.id.tv_searching);
        tvNoServices = mContent.findViewById(R.id.tv_no_services);
        btnSearchServices = mContent.findViewById(R.id.btn_search_services);
        btnBack = mContent.findViewById(R.id.btn_back);
        layoutServices = mContent.findViewById(R.id.container_services);

        tvSearching.setVisibility(View.VISIBLE);
        tvNoServices.setVisibility(View.GONE);
        btnSearchServices.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        layoutServices.setVisibility(View.GONE);
        layoutServices.removeAllViews();
    }

    private void startNetworkService() {
        Intent intent = new Intent(getContext(), NetworkService.class);
        getContext().startService(intent);
    }

    private void stopNetworkService() {
        Intent intent = new Intent(getContext(), NetworkService.class);
        getContext().stopService(intent);
    }

    private void onDiscoveryDone() {
        tvSearching.setVisibility(View.GONE);
        btnSearchServices.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);

        if (layoutServices.getChildCount() == 0) {
            tvNoServices.setVisibility(View.VISIBLE);
        }
    }

    private void onServiceFound(String service) {
        layoutServices.setVisibility(View.VISIBLE);
        layoutServices.addView(createListItemSeparator());
        layoutServices.addView(createServiceListItem(service));
    }

    private View createServiceListItem(final String service) {
        int padding = (int) getResources().getDimension(R.dimen.padding_medium);

        TextView vService = new TextView(getContext());
        vService.setText(service);
        vService.setTextSize(getResources().getDimension(R.dimen.textsize_medium));
        vService.setGravity(Gravity.CENTER);
        vService.setTextColor(getResources().getColor(R.color.dark));
        vService.setPadding(padding, padding, padding, padding);
        vService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNetworkService();
                PreferenceUtils.writeMasterServiceName(getContext(), service);
                mListener.onServiceSaved();
            }
        });
        return vService;
    }

    private View createListItemSeparator() {
        View vSeparator = new View(getContext());
        vSeparator.setBackgroundColor(getResources().getColor(R.color.dark));
        vSeparator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return vSeparator;
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new DiscoveryServiceFragment.DiscoveryMessageReceiver();
        }
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            getContext().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * BroadcastReceiver class that enables services to broadcastAction messages to this activity.
     */
    private class DiscoveryMessageReceiver extends BroadcastReceiver {

        public DiscoveryMessageReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Broadcast.SERVICE_NAME);
            intentFilter.addAction(Broadcast.DISCOVERY_DONE);
            getContext().registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case Broadcast.SERVICE_NAME:
                    onServiceFound(intent.getStringExtra(Broadcast.SERVICE_NAME));
                    break;
                case Broadcast.DISCOVERY_DONE:
                    onDiscoveryDone();
                    break;
            }
        }
    }

}
