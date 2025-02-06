package com.example.womensafety;

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
import android.os.IBinder;
import android.telephony.SmsManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.womensafety.R;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class ServiceMine extends Service {

    boolean isRunning = false;
    FusedLocationProviderClient fusedLocationClient;
    SmsManager manager = SmsManager.getDefault();
    String myLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        myLocation = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude();
                    } else {
                        myLocation = "Unable to Find Location :(";
                    }
                });

        // ShakeDetector to send SOS message when the device is shaken
        ShakeDetector.create(this, () -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String ENUM = sharedPreferences.getString("ENUM", "NONE");

            if (ENUM != null && !ENUM.equalsIgnoreCase("NONE")) {
                // Check if the location is available before sending
                if (myLocation != null && !myLocation.isEmpty()) {
                    manager.sendTextMessage(ENUM, null, "I'm in Trouble!\nSending My Location:\n" + myLocation, null, null);
                } else {
                    manager.sendTextMessage(ENUM, null, "I'm in Trouble!\nLocation unavailable", null, null);
                }
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase("STOP")) {
            if (isRunning) {
                this.stopForeground(true);
                this.stopSelf();
            }
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                m.createNotificationChannel(channel);

                Notification notification = new Notification.Builder(this, "MYID")
                        .setContentTitle("Women Safety")
                        .setContentText("Shake Device to Send SOS")
                        .setSmallIcon(R.drawable.girl_vector)
                        .setContentIntent(pendingIntent)
                        .build();
                this.startForeground(115, notification);
                isRunning = true;
                return START_NOT_STICKY;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ShakeDetector.stop();
        ShakeDetector.destroy();
    }
}