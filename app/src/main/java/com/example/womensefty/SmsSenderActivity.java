package com.example.womensefty;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.tbouron.shakedetector.library.ShakeDetector;

public class SmsSenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);  // Use your layout here
    }

    public void sendSms() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String number = sharedPreferences.getString("ENUM", "NONE");

        if (!number.equals("NONE")) {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "I'm in trouble! Here's my location: " + getLocation();  // Add actual location logic here
            try {
                smsManager.sendTextMessage(number, null, message, null, null);
                Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No valid number registered.", Toast.LENGTH_SHORT).show();
        }
    }

    // Simulating location fetching, replace with actual location logic
    private String getLocation() {
        return "http://maps.google.com/maps?q=loc:12.9716,77.5946"; // Replace with actual coordinates
    }



}
