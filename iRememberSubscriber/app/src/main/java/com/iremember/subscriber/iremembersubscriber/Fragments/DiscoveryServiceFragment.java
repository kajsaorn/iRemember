package com.iremember.subscriber.iremembersubscriber.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.Broadcast;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Services.NetworkService;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class DiscoveryServiceFragment extends Fragment {

    private View mContent;
    private OnServiceClickListener mListener;
    private DiscoveryMessageReceiver mBroadcastReceiver;
    private View tvSearching, tvNoServices, btnSearchServices, btnBack, btnInputIp, btnSaveIp;
    private EditText etIpAddress;
    private LinearLayout layoutServices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_discovery_services, container, false);
        return mContent;
    }

    @Override
    public void onResume() {
        super.onResume();
        initComponents();
        initListeners();
        registerBroadcastReceiver();
        startNetworkService();
    }

    @Override
    public void onStop() {
        unregisterBroadcastReceiver();
        stopNetworkService();
        super.onStop();
    }

    public interface OnServiceClickListener {
        void onServiceSaved();
        void onSearchServices();
        void onFinish();
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
                mListener.onSearchServices();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFinish();
            }
        });
        btnInputIp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showIpInputField();
            }
        });

        btnSaveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveIpAddress();
            }
        });
    }

    private void initComponents() {
        tvSearching = mContent.findViewById(R.id.tv_searching);
        tvNoServices = mContent.findViewById(R.id.tv_no_services);
        etIpAddress = mContent.findViewById(R.id.et_ip_address);
        btnSearchServices = mContent.findViewById(R.id.btn_search_services);
        btnBack = mContent.findViewById(R.id.btn_back);
        btnInputIp = mContent.findViewById(R.id.btn_input_ip);
        btnSaveIp = mContent.findViewById(R.id.btn_save_ip);
        layoutServices = mContent.findViewById(R.id.container_services);

        tvSearching.setVisibility(View.VISIBLE);
        tvNoServices.setVisibility(View.GONE);
        etIpAddress.setVisibility(View.GONE);
        btnSearchServices.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        btnInputIp.setVisibility(View.GONE);
        btnSaveIp.setVisibility(View.GONE);
        layoutServices.setVisibility(View.GONE);
        layoutServices.removeAllViews();
    }

    private void showIpInputField() {
        layoutServices.setVisibility(View.GONE);
        btnSearchServices.setVisibility(View.GONE);
        btnInputIp.setVisibility(View.GONE);
        btnSaveIp.setVisibility(View.VISIBLE);
        etIpAddress.setVisibility(View.VISIBLE);

        String mMasterServiceIp = PreferenceUtils.readMasterServiceIp(getContext());
        if (mMasterServiceIp != null) {
            etIpAddress.setText(mMasterServiceIp);
        }
    }

    private void saveIpAddress() {
        String ipAddress = etIpAddress.getText().toString();

        if (ipAddress == null && ipAddress.equals("")) {
            showUserMessage(UserMessage.MISSING_IP_ADDRESS);
            return;
        }
        PreferenceUtils.writeMasterIpAddress(getContext(), ipAddress);
        PreferenceUtils.writeMasterServiceName(getContext(), null);
        showUserMessage(UserMessage.SAVED_MASTER_SERVICE);
        mListener.onFinish();
    }

    private void onDiscoveryDone() {
        stopNetworkService();
        tvSearching.setVisibility(View.GONE);
        btnSearchServices.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnInputIp.setVisibility(View.VISIBLE);

        int visibility = (layoutServices.getChildCount() == 0) ? View.VISIBLE : View.GONE;
        tvNoServices.setVisibility(visibility);
    }

    private void onServiceFound(String service) {
        layoutServices.setVisibility(View.VISIBLE);
        layoutServices.addView(createServiceListItem(service));
        layoutServices.addView(createListItemSeparator());
    }

    private View createServiceListItem(final String service) {
        TextView vService = new TextView(getContext());
        vService.setText(service);
        vService.setTextSize(getResources().getDimension(R.dimen.textsize_medium));
        vService.setGravity(Gravity.CENTER);
        vService.setTextColor(getResources().getColor(R.color.dark));

        vService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceUtils.writeMasterServiceName(getContext(), service);
                showUserMessage(UserMessage.SAVED_MASTER_SERVICE);
                mListener.onServiceSaved();
            }
        });
        return vService;
    }

    private View createListItemSeparator() {
        int margin = (int) getResources().getDimension(R.dimen.margin_small);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, margin, 0, margin);

        View vSeparator = new View(getContext());
        vSeparator.setBackgroundColor(getResources().getColor(R.color.dark));
        vSeparator.setLayoutParams(params);
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
     * Start network service which, among other things, will look for available
     * remote iRemember Master Services. If one is found, this fragment will receive
     * a broadcast message and display the available service.
     */
    private void startNetworkService() {
        Intent intent = new Intent(getContext(), NetworkService.class);
        intent.putExtra(Broadcast.SEARCH_MASTER_SERVICE, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }
    }

    /**
     * Stop network service.
     */
    private void stopNetworkService() {
        Intent intent = new Intent(getContext(), NetworkService.class);
        getContext().stopService(intent);
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * BroadcastReceiver class that enables services to broadcast messages to this fragment.
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
