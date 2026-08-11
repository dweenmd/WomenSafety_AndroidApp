package com.dweenmd.womensafety.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationsSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);

        prefs = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar_notifications);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        MaterialSwitch switchSound = findViewById(R.id.switch_sos_sound);
        MaterialSwitch switchVibrate = findViewById(R.id.switch_sos_vibrate);
        MaterialSwitch switchFlashlight = findViewById(R.id.switch_sos_flashlight);

        switchSound.setChecked(prefs.getBoolean("sos_sound", true));
        switchVibrate.setChecked(prefs.getBoolean("sos_vibrate", true));
        switchFlashlight.setChecked(prefs.getBoolean("sos_flashlight", false));

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("sos_sound", isChecked).apply());
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("sos_vibrate", isChecked).apply());
        switchFlashlight.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("sos_flashlight", isChecked).apply());

        findViewById(R.id.btn_system_notification_settings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        });
    }
}
