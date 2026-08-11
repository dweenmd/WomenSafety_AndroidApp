package com.dweenmd.womensafety.ui.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dweenmd.womensafety.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Toolbar toolbar = findViewById(R.id.toolbar_verification);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        findViewById(R.id.verify_email).setOnClickListener(v -> {}); // Placeholder to prevent crash if layout logic changes
        findViewById(R.id.verify_phone).setOnClickListener(v -> {});

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserStatus();
    }

    private void refreshUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (isFinishing()) return;
                updateUI();
            });
        }
    }

    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        // Email Verification
        setupVerificationItem(findViewById(R.id.verify_email), android.R.drawable.ic_dialog_email, 
                "Email Verification", 
                user.isEmailVerified() ? "Verified" : "Tap to send verification email", 
                user.isEmailVerified(),
                () -> {
                    if (!user.isEmailVerified()) {
                        user.sendEmailVerification().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Failed to send email. Try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Email is already verified", Toast.LENGTH_SHORT).show();
                    }
                });

        // Phone Verification
        boolean hasPhone = user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty();
        setupVerificationItem(findViewById(R.id.verify_phone), android.R.drawable.ic_menu_call, 
                "Phone Verification", 
                hasPhone ? "Verified (" + user.getPhoneNumber() + ")" : "Phone number not linked", 
                hasPhone,
                () -> {
                    if (!hasPhone) {
                        // Redirect to Phone Login/Linking activity
                        startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.auth.PhoneLoginActivity.class));
                    } else {
                        Toast.makeText(this, "Phone is already verified", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupVerificationItem(View itemView, int iconRes, String title, String status, boolean isVerified, Runnable onClick) {
        ImageView ivIcon = itemView.findViewById(R.id.iv_perm_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_perm_title);
        TextView tvDesc = itemView.findViewById(R.id.tv_perm_description);
        ImageView ivStatus = itemView.findViewById(R.id.iv_perm_status);

        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvDesc.setText(status);

        if (isVerified) {
            ivStatus.setImageResource(R.drawable.ic_check);
            ivStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_safe_green)));
        } else {
            ivStatus.setImageResource(android.R.drawable.ic_dialog_alert);
            ivStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.m3_error)));
        }

        itemView.setOnClickListener(v -> onClick.run());
    }
}
