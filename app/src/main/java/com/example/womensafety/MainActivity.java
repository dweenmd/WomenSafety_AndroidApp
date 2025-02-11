package com.example.womensafety;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.location.Location;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private SmsManager smsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        smsManager = SmsManager.getDefault();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            m.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String num1 = sharedPreferences.getString("ENUM1", "NONE");
        String num2 = sharedPreferences.getString("ENUM2", "NONE");

        if (num1.equals("NONE") || num2.equals("NONE")) {
            startActivity(new Intent(this, RegisterNumberActivity.class));
        } else {
            TextView textView = findViewById(R.id.textNum);
            textView.setText("🚨 SOS Will Be Sent To 🚨\n📞 " + num1 + "\n📞 " + num2);
        }
    }

    public void sendPanicSMS(View view) {
        // Log when the method starts
        Log.d("VIBRATION_DEBUG", "sendPanicSMS triggered");

        // Vibrate when panic button is pressed
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            Log.d("VIBRATION_DEBUG", "Vibrator service found");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                Log.d("VIBRATION_DEBUG", "Vibration triggered (Android O+)");
            } else {
                vibrator.vibrate(500);
                Log.d("VIBRATION_DEBUG", "Vibration triggered (Legacy)");
            }
        } else {
            Log.e("VIBRATION_DEBUG", "Vibrator service not available");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String num1 = sharedPreferences.getString("ENUM1", "NONE");
        String num2 = sharedPreferences.getString("ENUM2", "NONE");

        if (num1.equals("NONE") || num2.equals("NONE")) {
            Toast.makeText(this, "No emergency numbers set!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            multiplePermissions.launch(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION});
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            String myLocation = (location != null) ?
                    "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude() :
                    "Location unavailable!";

            String message = "Emergency! I'm in trouble! \nPlease help me ASAP. \nMy current location: \n" + myLocation;

            smsManager.sendTextMessage(num1, null, message, null, null);
            smsManager.sendTextMessage(num2, null, message, null, null);

            Toast.makeText(MainActivity.this, "SOS Message Sent!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Failed to get location!", Toast.LENGTH_SHORT).show();
        });
    }

    private ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Grant Permission", v -> {
                                multiplePermissions.launch(new String[]{entry.getKey()});
                                snackbar.dismiss();
                            });
                            snackbar.show();
                        }
                    }
                }
            });

    public void stopService(View view) {
        Intent notificationIntent = new Intent(this, ServiceMine.class);
        notificationIntent.setAction("stop");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(notificationIntent);
            Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
        }
    }

    public void startServiceV(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent notificationIntent = new Intent(this, ServiceMine.class);
            notificationIntent.setAction("Start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(notificationIntent);
                Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
            }
        } else {
            multiplePermissions.launch(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    public void PopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.changeNum) {
                startActivity(new Intent(MainActivity.this, RegisterNumberActivity.class));
            }
            return true;
        });
        popupMenu.show();
    }

    public void callSOS999(View view) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:999"));
        startActivity(dialIntent);
    }

   /*
   // action call not working xiomi and samsung mobile.
    public void callSOS999(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:999"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        startActivity(callIntent);
    }
    */

    public void callSOS106(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:106"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        startActivity(callIntent);
    }

}
