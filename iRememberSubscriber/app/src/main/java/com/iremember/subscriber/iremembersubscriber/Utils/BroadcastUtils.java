package com.iremember.subscriber.iremembersubscriber.Utils;

import android.content.Context;
import android.content.Intent;

public class BroadcastUtils {

    public static void broadcast(String action, Context context) {
            Intent intent = new Intent();
            intent.setAction(action);
            context.sendBroadcast(intent);
    }
}
