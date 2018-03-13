package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.iremember.subscriber.iremembersubscriber.BuildConfig;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;

public class PreferenceUtils {

    public static void writeRoomName(Context context, String roomName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.ROOM_NAME, roomName).commit();
    }

    public static String readRoomName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.ROOM_NAME, "");
    }

    public static void writeBackgroundColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.BACKGROUND_COLOR, color).commit();
    }

    public static int readBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.BACKGROUND_COLOR, 0);
    }

    public static void writeTextColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.TEXT_COLOR, color).commit();
    }

    public static int readTextColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.TEXT_COLOR, 0);
    }

}
