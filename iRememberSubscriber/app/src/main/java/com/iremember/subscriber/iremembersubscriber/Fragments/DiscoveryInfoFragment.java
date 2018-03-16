package com.iremember.subscriber.iremembersubscriber.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iremember.subscriber.iremembersubscriber.R;

public class DiscoveryInfoFragment extends Fragment {

    View mContent;
    OnStartConfigurationListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_disovery_info, container, false);
        setListeners();
        return mContent;
    }

    private void setListeners() {
        View btnStart = mContent.findViewById(R.id.btn_start_configuration);

        btnStart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onStartConfiguration();
            }
        });
    }

    public interface OnStartConfigurationListener {
        void onStartConfiguration();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnStartConfigurationListener) getActivity();
    }
}
