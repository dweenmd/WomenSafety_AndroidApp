package com.example.womensafety;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class SmsSenderActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);  // Use your layout

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request permissions at runtime
        requestPermissions();
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied! SMS and location won't work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void sendSms() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String num1 = sharedPreferences.getString("ENUM1", "NONE");
        String num2 = sharedPreferences.getString("ENUM2", "NONE");

        if (!num1.equals("NONE") && !num2.equals("NONE")) {
            getLocationAndSendSms(num1, num2);
        } else {
            Toast.makeText(this, "No valid number registered.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationAndSendSms(String num1, String num2) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permissions not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                requestNewLocation(num1, num2);
                return;
            }

            String locationUrl = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude();
            sendSmsToContacts(num1, num2, locationUrl);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void requestNewLocation(String num1, String num2) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 seconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                if (locationResult == null) {
                    sendSmsToContacts(num1, num2, "Location unavailable!");
                    return;
                }
                Location location = locationResult.getLastLocation();
                String locationUrl = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude();
                sendSmsToContacts(num1, num2, locationUrl);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void sendSmsToContacts(String num1, String num2, String locationUrl) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "I'm in Trouble! Sending My Location: " + locationUrl;
            smsManager.sendTextMessage(num1, null, message, null, null);
            smsManager.sendTextMessage(num2, null, message, null, null);
            Toast.makeText(this, "SOS Message Sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
