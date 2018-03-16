package com.iremember.master.iremembermaster.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.iremember.master.iremembermaster.BuildConfig;
import com.iremember.master.iremembermaster.Constants.SharedPrefs;

import java.util.Map;


public class PreferenceUtils {

    public static void writeMasterName(Context context, String masterName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.MASTER_NAME, masterName).commit();
    }

    public static String readMasterName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.MASTER_NAME, null);
    }

    /**
     * Adds or update a subscriber
     * @param context
     * @param roomName
     * @param ipAndPort
     */
    public static void writeSubscriber(Context context, String roomName, String ipAndPort) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_RECEIVER, Context.MODE_PRIVATE);
        prefs.edit().putString(roomName, ipAndPort).commit();
    }

    public static void removeSubscriber(Context context, String roomName) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_RECEIVER, Context.MODE_PRIVATE);
        prefs.edit().remove(roomName).commit();
    }

    public static Map<String, ?> getAllSubscribers(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPrefs.PREFS_RECEIVER, Context.MODE_PRIVATE);
        return prefs.getAll();
    }

    public static void writeNetworkServiceRunState(Context context, boolean runStatus) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPrefs.NETWORK_SERVICE_RUN_STATE, runStatus).commit();
    }

    public static boolean readNetworkServiceRunState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPrefs.NETWORK_SERVICE_RUN_STATE, false);
    }
}
