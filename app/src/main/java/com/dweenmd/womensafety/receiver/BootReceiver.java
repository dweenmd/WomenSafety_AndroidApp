package com.dweenmd.womensafety.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.service.SosForegroundService;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted. Checking if protection should be enabled.");
            
            // Check if user explicitly had protection enabled before boot.
            // For now, we check if they have contacts saved. If they do, we assume protection is ON.
            // In a fuller implementation, this would check a 'backgroundProtection' flag in shared prefs/Firestore.
            
            SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            boolean protectionEnabled = prefs.getBoolean("backgroundProtection", true); // Default to true if not set
            
            if (protectionEnabled) {
                ContactsRepository contactsRepo = new ContactsRepository(context);
                List<ContactsRepository.Contact> contacts = contactsRepo.getLocalContactsSync();
                
                if (contacts != null && !contacts.isEmpty()) {
                    Log.d(TAG, "Protection enabled and contacts exist. Starting SosForegroundService.");
                    Intent serviceIntent = new Intent(context, SosForegroundService.class);
                    serviceIntent.setAction("Start");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
                } else {
                    Log.d(TAG, "Protection enabled, but no contacts exist. Not starting service.");
                }
            } else {
                Log.d(TAG, "Protection explicitly disabled by user. Not starting service.");
            }
        }
    }
}
