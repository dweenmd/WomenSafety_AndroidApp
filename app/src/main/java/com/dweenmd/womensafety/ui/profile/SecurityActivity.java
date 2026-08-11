package com.dweenmd.womensafety.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecurityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        Toolbar toolbar = findViewById(R.id.toolbar_security);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupMenuItem(findViewById(R.id.menu_change_password), android.R.drawable.ic_lock_idle_lock, 
                "Change Password", "Update your account password", v -> sendPasswordResetEmail());
        
        setupMenuItem(findViewById(R.id.menu_active_sessions), android.R.drawable.ic_menu_agenda, 
                "Active Sessions", "View devices currently logged in", v -> showToast("Feature coming soon"));
        
        setupMenuItem(findViewById(R.id.menu_login_activity_sec), android.R.drawable.ic_menu_recent_history, 
                "Login Activity", "View recent account activity", v -> startActivity(new Intent(this, LoginActivityHistoryActivity.class)));
        
        setupMenuItem(findViewById(R.id.menu_trusted_devices), android.R.drawable.ic_lock_idle_lock, 
                "Trusted Devices", "Manage trusted devices", v -> showToast("Feature coming soon"));

        SwitchMaterial switch2Fa = findViewById(R.id.switch_2fa);
        switch2Fa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast(isChecked ? "2FA Setup started" : "2FA Disabled");
        });

        SwitchMaterial switchBiometric = findViewById(R.id.switch_biometric);
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast(isChecked ? "Biometric Enabled" : "Biometric Disabled");
        });

        findViewById(R.id.btn_logout_all).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Log Out From All Devices")
                    .setMessage("Are you sure you want to log out from all devices?")
                    .setPositiveButton("Log Out All", (dialog, which) -> {
                        showToast("Logged out of all devices");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void sendPasswordResetEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Password reset email sent to " + user.getEmail());
                        } else {
                            showToast("Failed to send reset email");
                        }
                    });
        } else {
            showToast("No email associated with this account");
        }
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
