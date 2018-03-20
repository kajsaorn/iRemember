package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

import com.iremember.subscriber.iremembersubscriber.BuildConfig;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;
import com.iremember.subscriber.iremembersubscriber.Constants.UserMessage;
import com.iremember.subscriber.iremembersubscriber.R;

import java.lang.reflect.Field;

public class PreferenceUtils {

    public static void writeMasterServiceName(Context context, String serviceName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.MASTER_SERVICE, serviceName).commit();
    }

    public static String readMasterServiceName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.MASTER_SERVICE, null);
    }

    public static void writeRoomName(Context context, String roomName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.MY_ROOM_NAME, roomName).commit();
    }

    public static String readRoomName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.MY_ROOM_NAME, null);
    }

    public static void writeAllowReminders(Context context, boolean allow) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPrefs.ALLOW_REMINDERS, allow).commit();
    }

    public static boolean readRemindersAllowed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPrefs.ALLOW_REMINDERS, true);
    }

    public static void writeAllowMusic(Context context, boolean allow) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPrefs.ALLOW_MUSIC, allow).commit();
    }

    public static boolean readMusicAllowed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPrefs.ALLOW_MUSIC, true);
    }

    public static void writeBackgroundColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.MY_BACKGROUND_COLOR, color).commit();
    }

    public static int readBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.MY_BACKGROUND_COLOR, defaultBackgroundColor(context));
    }

    public static void writeTextColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.MY_TEXT_COLOR, color).commit();
    }

    public static int readTextColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.MY_TEXT_COLOR, defaultTextColor(context));
    }

    public static void writeSongTitle(Context context, String songTitle) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.MY_SONG_TITLE, songTitle).commit();
    }

    public static String readSongTitle(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.MY_SONG_TITLE, defaultSongTitle());
    }

    public static int defaultBackgroundColor(Context context) {
        return ResourcesCompat.getColor(context.getResources(), R.color.default_reminder_bg_color, null);
    }

    public static int defaultTextColor(Context context) {
        return ResourcesCompat.getColor(context.getResources(), R.color.default_reminder_text_color, null);
    }

    public static String defaultSongTitle() {
        Field[] songs = R.raw.class.getFields();
        return (songs.length > 0) ? songs[0].getName() : null;
    }

    /**
     * Display message to user as Android Toast.
     */
    public static void showUserConfirmation(Context context) {
        Toast.makeText(context, UserMessage.SAVED_SETTINGS, Toast.LENGTH_SHORT).show();
    }
}
