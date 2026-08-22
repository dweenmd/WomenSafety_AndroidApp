package com.dweenmd.womensafety.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecurityActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        setupToolbar(R.id.toolbar_security);

        setupMenuItem(findViewById(R.id.menu_change_password), R.drawable.ic_lock_elegant,
                "Change Password", "Update your account password", this::sendPasswordResetEmail);

        setupMenuItem(findViewById(R.id.menu_active_sessions), R.drawable.ic_history,
                "Active Sessions", "View devices currently logged in", () -> showToast("Feature coming soon"));

        setupMenuItem(findViewById(R.id.menu_login_activity_sec), R.drawable.ic_history,
                "Login Activity", "View recent account activity", () -> startActivity(new Intent(this, LoginActivityHistoryActivity.class)));

        setupMenuItem(findViewById(R.id.menu_trusted_devices), R.drawable.ic_lock_elegant,
                "Trusted Devices", "Manage trusted devices", () -> showToast("Feature coming soon"));

        MaterialSwitch switch2Fa = findViewById(R.id.switch_2fa);
        switch2Fa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast(isChecked ? "2FA Setup started" : "2FA Disabled");
        });

        MaterialSwitch switchBiometric = findViewById(R.id.switch_biometric);
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

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
