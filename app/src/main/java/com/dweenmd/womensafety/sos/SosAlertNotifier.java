package com.dweenmd.womensafety.sos;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

/**
 * Plays the user-configured feedback (sound / vibration / flashlight) when an
 * SOS fires. The toggles live in the "NotificationPrefs" SharedPreferences and
 * are set from NotificationsSettingsActivity.
 */
public class SosAlertNotifier {

    private static final String TAG = "SosAlertNotifier";
    private static final String PREFS_NAME = "NotificationPrefs";

    private static final long[] VIBRATE_PATTERN = {0, 600, 200, 600, 200, 600};
    private static final long TORCH_DURATION_MS = 5000;

    public static void notify(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (prefs.getBoolean("sos_vibrate", true)) {
            vibrate(context);
        }
        if (prefs.getBoolean("sos_sound", true)) {
            playAlarm(context);
        }
        if (prefs.getBoolean("sos_flashlight", false)) {
            flashTorch(context);
        }
    }

    private static void vibrate(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, -1));
        } catch (Exception e) {
            Log.w(TAG, "Vibration failed", e);
        }
    }

    private static void playAlarm(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            if (alarmUri == null) return;
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            if (ringtone != null) {
                ringtone.setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .build());
                ringtone.play();
            }
        } catch (Exception e) {
            Log.w(TAG, "Alarm sound failed", e);
        }
    }

    private static void flashTorch(Context context) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) return;

            for (String cameraId : cameraManager.getCameraIdList()) {
                if (cameraManager.getCameraCharacteristics(cameraId)
                        .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    cameraManager.setTorchMode(cameraId, true);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            cameraManager.setTorchMode(cameraId, false);
                        } catch (CameraAccessException e) {
                            Log.w(TAG, "Could not turn torch off", e);
                        }
                    }, TORCH_DURATION_MS);
                    return;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Torch failed", e);
        }
    }
}
