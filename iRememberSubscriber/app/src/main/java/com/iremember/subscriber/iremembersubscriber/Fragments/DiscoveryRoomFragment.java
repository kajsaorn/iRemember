package com.iremember.subscriber.iremembersubscriber.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class DiscoveryRoomFragment extends Fragment {

    View mContent;
    OnRoomNameSavedListener mListener;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_discovery_room, container, false);
        setListeners();
        setRoomName();
        return mContent;
    }

    private void setListeners() {
        View btnSave = mContent.findViewById(R.id.btn_save_room_name);

        btnSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mRoomName = ((EditText) mContent.findViewById(R.id.et_room_name)).getText().toString();

                if (mRoomName == null || mRoomName.trim().equals("")) {
                    showUserMessage(UserMessage.MISSING_ROOM_NAME);
                } else {
                    PreferenceUtils.writeRoomName(getContext(), mRoomName);
                    mListener.onRoomNameSaved();
                }
            }
        });
    }

    private void setRoomName() {
        String mRoomName = PreferenceUtils.readRoomName(getContext());

        if (mRoomName != null) {
            ((EditText) mContent.findViewById(R.id.et_room_name)).setText(mRoomName);
        }
    }

    public interface OnRoomNameSavedListener {
        void onRoomNameSaved();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnRoomNameSavedListener) getActivity();
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}
