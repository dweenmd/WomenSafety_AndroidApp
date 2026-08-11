package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;

public class SecurityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        Toolbar toolbar = findViewById(R.id.toolbar_security);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupMenuItem(findViewById(R.id.menu_change_password), android.R.drawable.ic_lock_idle_lock, "Change Password", "Update your account password", v -> showToast("Change Password Clicked"));
        setupMenuItem(findViewById(R.id.menu_active_sessions), android.R.drawable.ic_menu_agenda, "Active Sessions", "View devices currently logged in", v -> showToast("Active Sessions Clicked"));
        setupMenuItem(findViewById(R.id.menu_login_activity_sec), android.R.drawable.ic_menu_recent_history, "Login Activity", "View recent account activity", v -> showToast("Login Activity Clicked"));
        setupMenuItem(findViewById(R.id.menu_trusted_devices), android.R.drawable.ic_lock_idle_lock, "Trusted Devices", "Manage trusted devices", v -> showToast("Trusted Devices Clicked"));

        com.google.android.material.switchmaterial.SwitchMaterial switch2Fa = findViewById(R.id.switch_2fa);
        switch2Fa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("2FA Setup Flow Started");
            } else {
                showToast("2FA Disabled");
            }
        });

        com.google.android.material.switchmaterial.SwitchMaterial switchBiometric = findViewById(R.id.switch_biometric);
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("Biometric Authentication Enabled");
            } else {
                showToast("Biometric Authentication Disabled");
            }
        });

        findViewById(R.id.btn_logout_all).setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Log Out From All Devices")
                    .setMessage("Are you sure you want to log out from all devices? You will be logged out of this device as well.")
                    .setPositiveButton("Log Out All", (dialog, which) -> {
                        showToast("Logged out of all devices");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupMenuItem(View menuItem, int iconRes, String title, String subtitle, View.OnClickListener onClickListener) {
        if (menuItem == null) return;
        ImageView ivIcon = menuItem.findViewById(R.id.iv_menu_icon);
        TextView tvTitle = menuItem.findViewById(R.id.tv_menu_title);
        TextView tvSubtitle = menuItem.findViewById(R.id.tv_menu_subtitle);
        
        if (ivIcon != null) ivIcon.setImageResource(iconRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        }
        
        menuItem.setOnClickListener(onClickListener);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
