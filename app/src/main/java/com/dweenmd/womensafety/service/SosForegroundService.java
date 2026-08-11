package com.dweenmd.womensafety.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
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
    
    // Cooldown mechanism to prevent SMS spam on continuous shaking
    private boolean isOnCooldown = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long COOLDOWN_MS = 10000; // 10 seconds cooldown

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sosMessenger = new SosMessenger(this);

        ShakeDetector.create(this, () -> {
            if (!isOnCooldown) {
                Log.d(TAG, "Shake detected! Triggering SOS...");
                triggerSos();
                startCooldown();
            } else {
                Log.d(TAG, "Shake detected but on cooldown. Ignored.");
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

    private android.content.BroadcastReceiver powerButtonReceiver;
    private int powerButtonPressCount = 0;
    private long lastPowerButtonPressTime = 0;
    private static final int POWER_BUTTON_PRESS_THRESHOLD = 3; // Trigger after 3 presses
    private static final long POWER_BUTTON_TIME_WINDOW = 3000; // 3 seconds window

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "stop".equalsIgnoreCase(intent.getAction())) {
            if (isRunning) {
                stopForeground(true);
                stopSelf();
                isRunning = false;
            }
        } else {
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
                    .setContentText("Background protection is running. Shake or press Power button 3x to trigger SOS.")
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
        }

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
                                    Log.d(TAG, "Power button sequence detected! Triggering SOS...");
                                    triggerSos();
                                    startCooldown();
                                }
                                powerButtonPressCount = 0; // Reset
                            }
                        }
                        lastPowerButtonPressTime = currentTime;
                    }
                }
            };
            android.content.IntentFilter filter = new android.content.IntentFilter();
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
    }
}
