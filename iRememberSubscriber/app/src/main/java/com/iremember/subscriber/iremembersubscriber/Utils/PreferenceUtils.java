package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.iremember.subscriber.iremembersubscriber.BuildConfig;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;

public class PreferenceUtils {

    public static String readRoomName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.ROOM_NAME, "");
    }

    public static void writeRoomName(Context context, String roomName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.ROOM_NAME, roomName).commit();
    }
}
