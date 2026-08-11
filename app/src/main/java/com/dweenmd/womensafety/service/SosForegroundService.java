package com.dweenmd.womensafety.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.WomenSafetyApp;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.dweenmd.womensafety.ui.MainActivity;
import com.github.tbouron.shakedetector.library.ShakeDetector;

public class SosForegroundService extends Service {

    private static final String TAG = "SosForegroundService";
    private boolean isRunning = false;
    private SosMessenger sosMessenger;
    private SharedPreferences prefs;
    
    private boolean isOnCooldown = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long COOLDOWN_MS = 10000;

    private android.content.BroadcastReceiver batteryReceiver;
    private boolean lowBatteryAlertSent = false;

    private android.content.BroadcastReceiver powerButtonReceiver;
    private int powerButtonPressCount = 0;
    private long lastPowerButtonPressTime = 0;
    private static final int POWER_BUTTON_PRESS_THRESHOLD = 3;
    private static final long POWER_BUTTON_TIME_WINDOW = 3000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sosMessenger = new SosMessenger(this);
        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        ShakeDetector.create(this, () -> {
            boolean shakeEnabled = prefs.getBoolean("shakeDetection", true);
            if (shakeEnabled && !isOnCooldown) {
                Log.d(TAG, "Shake detected! Triggering SOS...");
                triggerSos();
                startCooldown();
            }
        });
        
        registerBatteryReceiver();
    }

    private void registerBatteryReceiver() {
        batteryReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean autoNotify = prefs.getBoolean("autoNotify", false);
                if (!autoNotify) return;

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                if (batteryPct <= 15 && !lowBatteryAlertSent) {
                    sendLowBatteryAlert();
                    lowBatteryAlertSent = true;
                } else if (batteryPct > 20) {
                    lowBatteryAlertSent = false;
                }
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void sendLowBatteryAlert() {
        sosMessenger.shareLocationOnly(new SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                Log.d(TAG, "Low battery location shared: " + status);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Low battery alert failed: " + error);
            }
        });
    }

    private void startCooldown() {
        isOnCooldown = true;
        handler.postDelayed(() -> isOnCooldown = false, COOLDOWN_MS);
    }

    private void triggerSos() {
        sosMessenger.triggerSos(new SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                Log.d(TAG, "SOS Triggered: " + status);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "SOS Failed: " + error);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "stop".equalsIgnoreCase(intent.getAction())) {
            if (isRunning) {
                stopForeground(true);
                stopSelf();
                isRunning = false;
            }
            return START_NOT_STICKY;
        }

        if (isRunning) return START_STICKY;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, WomenSafetyApp.CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notification = builder
                .setContentTitle("Women Safety Active")
                .setContentText("Background protection is running. Shake or press Power button 3x for SOS.")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                serviceType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            }
            startForeground(115, notification, serviceType);
        } else {
            startForeground(115, notification);
        }

        ShakeDetector.start();
        registerPowerButtonReceiver();
        isRunning = true;

        return START_STICKY;
    }
    
    private void registerPowerButtonReceiver() {
        if (powerButtonReceiver == null) {
            powerButtonReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) || Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastPowerButtonPressTime > POWER_BUTTON_TIME_WINDOW) {
                            powerButtonPressCount = 1;
                        } else {
                            powerButtonPressCount++;
                            if (powerButtonPressCount >= POWER_BUTTON_PRESS_THRESHOLD) {
                                if (!isOnCooldown) {
                                    triggerSos();
                                    startCooldown();
                                }
                                powerButtonPressCount = 0;
                            }
                        }
                        lastPowerButtonPressTime = currentTime;
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(powerButtonReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ShakeDetector.stop();
        ShakeDetector.destroy();
        handler.removeCallbacksAndMessages(null);
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver);
            powerButtonReceiver = null;
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        isRunning = false;
    }
}
