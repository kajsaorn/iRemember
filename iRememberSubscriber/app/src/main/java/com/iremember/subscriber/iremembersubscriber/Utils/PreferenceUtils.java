package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import com.iremember.subscriber.iremembersubscriber.BuildConfig;
import com.iremember.subscriber.iremembersubscriber.Constants.SharedPrefs;
import com.iremember.subscriber.iremembersubscriber.R;

import java.lang.reflect.Field;

public class PreferenceUtils {

    public static void writeRoomName(Context context, String roomName) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.ROOM_NAME, roomName).commit();
    }

    public static String readRoomName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.ROOM_NAME, null);
    }

    public static void writeBackgroundColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.BACKGROUND_COLOR, color).commit();
    }

    public static int readBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.BACKGROUND_COLOR, defaultBackgroundColor(context));
    }

    public static void writeTextColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putInt(SharedPrefs.TEXT_COLOR, color).commit();
    }

    public static int readTextColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getInt(SharedPrefs.TEXT_COLOR, defaultTextColor(context));
    }

    public static void writeSongTitle(Context context, String songTitle) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(SharedPrefs.SONG_TITLE, songTitle).commit();
    }

    public static String readSongTitle(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return prefs.getString(SharedPrefs.SONG_TITLE, defaultSongTitle());
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
}
