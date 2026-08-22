package com.dweenmd.womensafety.ui.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        setupToolbar(R.id.toolbar_verification);

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
        
        boolean isEmailProvider = false;
        boolean isGoogleProvider = false;
        boolean isPhoneProvider = false;

        for (com.google.firebase.auth.UserInfo userInfo : user.getProviderData()) {
            String pid = userInfo.getProviderId();
            if (pid.equals("google.com")) isGoogleProvider = true;
            else if (pid.equals("password")) isEmailProvider = true;
            else if (pid.equals("phone")) isPhoneProvider = true;
        }

        // Email Verification: only truly verified accounts (or Google sign-in,
        // which verifies upstream) count. Password sign-in alone must NOT show
        // as verified — that defeated the whole purpose.
        boolean emailVerified = user.isEmailVerified() || isGoogleProvider;

        setupVerificationItem(findViewById(R.id.verify_email), R.drawable.ic_email_elegant,
                "Email Verification",
                emailVerified ? "Verified (" + user.getEmail() + ")" : "Tap to send verification email",
                emailVerified,
                () -> {
                    if (emailVerified) {
                        Toast.makeText(this, "Email already verified", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (user.getEmail() == null) {
                        Toast.makeText(this, "Please add an email to your account", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new com.dweenmd.womensafety.data.AuthRepository(this).sendVerificationEmail(
                            new com.dweenmd.womensafety.data.AuthRepository.AuthCallback() {
                                @Override
                                public void onSuccess(com.google.firebase.auth.FirebaseUser u) {
                                    Toast.makeText(VerificationActivity.this,
                                            "Verification email sent to " + u.getEmail() + " — check your inbox (and spam)", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(VerificationActivity.this,
                                            "Could not send: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                });

        // Phone Verification Logic
        // Verified if signed in via phone or has a phone number linked
        boolean phoneVerified = isPhoneProvider || (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty());
        
        setupVerificationItem(findViewById(R.id.verify_phone), R.drawable.ic_phone, 
                "Phone Verification", 
                phoneVerified ? "Verified (" + user.getPhoneNumber() + ")" : "Not Linked", 
                phoneVerified,
                () -> {
                    if (!phoneVerified) {
                        startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.auth.PhoneLoginActivity.class));
                    } else {
                        Toast.makeText(this, "Phone is verified", Toast.LENGTH_SHORT).show();
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
            ivStatus.setImageResource(R.drawable.ic_warning);
            ivStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.m3_error)));
        }

        itemView.setOnClickListener(v -> onClick.run());
    }
}
