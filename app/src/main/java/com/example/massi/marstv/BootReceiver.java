package com.example.massi.marstv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class BootReceiver extends WakefulBroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Log.e("BootReceiver", "HERE");
        Intent startServiceIntent = new Intent(context, PostMetrics.class);
        startWakefulService(context, startServiceIntent);
    }
}
