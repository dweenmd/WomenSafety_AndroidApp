package com.dweenmd.womensafety.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationRepository {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationRepository(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(LocationCallbackResult callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure("Location permissions not granted.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                callback.onSuccess(location);
            } else {
                // Fallback to request new location
                requestFreshLocation(callback);
            }
        }).addOnFailureListener(e -> {
            // Fallback to request new location
            requestFreshLocation(callback);
        });
    }

    private void requestFreshLocation(LocationCallbackResult callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure("Location permissions not granted.");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();
                
        final boolean[] callbackTriggered = {false};

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (callbackTriggered[0]) return;
                callbackTriggered[0] = true;
                
                fusedLocationClient.removeLocationUpdates(this);
                
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    callback.onFailure("Unable to retrieve fresh location within timeout.");
                } else {
                    callback.onSuccess(locationResult.getLastLocation());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        
        // Explicit timeout fallback
        new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!callbackTriggered[0]) {
                callbackTriggered[0] = true;
                fusedLocationClient.removeLocationUpdates(locationCallback);
                callback.onFailure("Location request timed out.");
            }
        }, 5000); // 5 seconds timeout
    }

    public interface LocationCallbackResult {
        void onSuccess(Location location);
        void onFailure(String reason);
    }
}
