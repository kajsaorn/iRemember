package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.LinkedList;

public class BroadcastUtils {

    public static void broadcastAction(String action, Context context) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }

    public static void broadcastString(String action, String data, Context context) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(action, data);
        context.sendBroadcast(intent);
    }

    public static void broadcastList(String action, ArrayList<String> list, Context context) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(action, list);
        context.sendBroadcast(intent);
    }
}
