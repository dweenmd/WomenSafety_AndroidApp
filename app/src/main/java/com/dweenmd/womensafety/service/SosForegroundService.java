package com.dweenmd.womensafety.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.content.pm.ServiceInfo;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class SosForegroundService extends Service {

    boolean isRunning = false;
    FusedLocationProviderClient fusedLocationClient;
    SmsManager manager = SmsManager.getDefault();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ShakeDetector.create(this, () -> {
            getLocationAndSendSms();
        });
    }

    private void getLocationAndSendSms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            String myLocation = (location != null) ?
                    "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude() :
                    "Location unavailable!";

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String num1 = sharedPreferences.getString("ENUM1", "NONE");
            String num2 = sharedPreferences.getString("ENUM2", "NONE");

            if (!num1.equals("NONE") && !num2.equals("NONE")) {
                String message = "Emergency! I'm in trouble!\nPlease help me ASAP.\nMy current location: " + myLocation;
                manager.sendTextMessage(num1, null, message, null, null);
                manager.sendTextMessage(num2, null, message, null, null);

                Toast.makeText(this, "SOS Message Sent!", Toast.LENGTH_SHORT).show();
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
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (m != null) {
                    m.createNotificationChannel(channel);
                }

                Notification notification = new Notification.Builder(this, "MYID")
                        .setContentTitle("Women Safety")
                        .setContentText("Shake Device to Send SOS")
                        .setSmallIcon(R.drawable.logo)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(115, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                } else {
                    startForeground(115, notification);
                }
                
                isRunning = true;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ShakeDetector.stop();
        ShakeDetector.destroy();
    }
}
