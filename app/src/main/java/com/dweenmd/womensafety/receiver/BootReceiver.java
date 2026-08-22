package com.dweenmd.womensafety.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.dweenmd.womensafety.service.SosForegroundService;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted. Checking if protection should be re-enabled.");

            SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            boolean protectionEnabled = prefs.getBoolean("backgroundProtection", true);

            if (protectionEnabled) {
                // Android 14+ forbids apps from starting a location foreground service
                // from BOOT_COMPLETED, so record the intent here; MainActivity restarts
                // the service the next time the app comes to the foreground.
                prefs.edit().putBoolean(SosForegroundService.KEY_RESTART_PENDING, true).apply();
                Log.d(TAG, "Protection was enabled. Service will restart when app is opened.");
            } else {
                Log.d(TAG, "Protection explicitly disabled by user. Not restarting service.");
            }
        }
    }
}
