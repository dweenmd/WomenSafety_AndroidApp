package com.dweenmd.womensafety.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ServiceCompat;

import com.dweenmd.womensafety.ui.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uber-style live location sharing: while active, the phone's position is
 * uploaded to Firestore every UPDATE_INTERVAL_MS so anyone with the session
 * link can follow along in real time (web/live.html).
 */
public class LiveLocationService extends Service {

    private static final String TAG = "LiveLocationService";
    public static final String ACTION_STOP = "com.dweenmd.womensafety.LIVE_STOP";
    public static final String EXTRA_SESSION_ID = "sessionId";
    public static final String EXTRA_OWNER_NAME = "ownerName";
    public static final String PREF_SESSION_ID = "live_session_id";

    private static final int NOTIFICATION_ID = 116;
    private static final String CHANNEL_ID = "live_location_channel";
    private static final long UPDATE_INTERVAL_MS = 15_000;
    private static final long SESSION_DURATION_MS = 8L * 60 * 60 * 1000; // 8 hours
    private static final int MAX_PATH_POINTS = 120;

    private FusedLocationProviderClient fusedClient;
    private FirebaseFirestore db;
    private String sessionId;
    private String ownerName;
    private long startedAt;
    private final List<Map<String, Object>> path = new ArrayList<>();

    public static boolean isSharing(Context context) {
        return context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .contains(PREF_SESSION_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSharing();
            return START_NOT_STICKY;
        }

        if (sessionId != null) return START_STICKY; // already running

        sessionId = (intent != null) ? intent.getStringExtra(EXTRA_SESSION_ID) : null;
        ownerName = (intent != null) ? intent.getStringExtra(EXTRA_OWNER_NAME) : null;
        if (sessionId == null) {
            Log.e(TAG, "Started without a sessionId; stopping");
            stopSelf();
            return START_NOT_STICKY;
        }

        startedAt = System.currentTimeMillis();
        startAsForeground();
        persistSessionId(sessionId);
        requestLocationUpdates();

        return START_STICKY;
    }

    private void startAsForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Live location sharing", NotificationManager.IMPORTANCE_LOW);
        nm.createNotificationChannel(channel);

        Intent stopIntent = new Intent(this, LiveLocationService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Sharing live location")
                .setContentText("Your trusted contacts can see where you are in real time.")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .setContentIntent(openPi)
                .addAction(new Notification.Action.Builder(null, "Stop sharing", stopPi).build())
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS / 2)
                .build();

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            // Location permission revoked while sharing — end the session cleanly.
            Log.e(TAG, "Location permission missing", e);
            stopSharing();
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult result) {
            Location location = result.getLastLocation();
            if (location != null) {
                uploadPoint(location.getLatitude(), location.getLongitude());
            }
        }
    };

    private void uploadPoint(double lat, double lng) {
        Map<String, Object> point = new HashMap<>();
        point.put("lat", lat);
        point.put("lng", lng);
        point.put("t", System.currentTimeMillis());
        path.add(point);
        if (path.size() > MAX_PATH_POINTS) {
            path.subList(0, path.size() - MAX_PATH_POINTS).clear();
        }

        Map<String, Object> doc = new HashMap<>();
        doc.put("latest", point);
        doc.put("path", new ArrayList<>(path));
        doc.put("active", true);
        doc.put("updatedAt", System.currentTimeMillis());
        doc.put("startedAt", startedAt);
        doc.put("expiresAt", startedAt + SESSION_DURATION_MS);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            doc.put("ownerUid", user.getUid());
        }
        if (ownerName != null) {
            doc.put("ownerName", ownerName);
        }

        db.collection("live_sessions").document(sessionId)
                .set(doc, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload location", e));
    }

    private void persistSessionId(String id) {
        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .edit().putString(PREF_SESSION_ID, id).apply();
    }

    private void stopSharing() {
        if (sessionId != null) {
            Map<String, Object> end = new HashMap<>();
            end.put("active", false);
            end.put("updatedAt", System.currentTimeMillis());
            db.collection("live_sessions").document(sessionId)
                    .set(end, com.google.firebase.firestore.SetOptions.merge())
                    .addOnFailureListener(e -> Log.w(TAG, "Could not mark session inactive", e));
        }
        try {
            fusedClient.removeLocationUpdates(locationCallback);
        } catch (Exception ignored) {
        }
        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .edit().remove(PREF_SESSION_ID).apply();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        try {
            fusedClient.removeLocationUpdates(locationCallback);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}
