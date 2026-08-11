package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.R;
import com.google.android.material.button.MaterialButton;

public class PrivacyConsentActivity extends AppCompatActivity {

    private static final String PREF_CONSENT = "privacy_consent_prefs";
    private static final String KEY_CONSENT_TIMESTAMP = "consent_timestamp";
    private static final String KEY_CONSENT_VERSION = "consent_version";
    private static final int CURRENT_CONSENT_VERSION = 1;
    
    // TODO: Update with real hosted privacy policy URL before release
    private static final String PRIVACY_POLICY_URL = "https://example.com/privacy-policy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_consent);

        MaterialButton btnAgree = findViewById(R.id.btn_agree);
        TextView tvPrivacyPolicyLink = findViewById(R.id.tv_privacy_policy_link);

        btnAgree.setOnClickListener(v -> {
            saveConsent();
            proceedToLogin();
        });

        tvPrivacyPolicyLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL));
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveConsent() {
        SharedPreferences prefs = getSharedPreferences(PREF_CONSENT, MODE_PRIVATE);
        prefs.edit()
             .putLong(KEY_CONSENT_TIMESTAMP, System.currentTimeMillis())
             .putInt(KEY_CONSENT_VERSION, CURRENT_CONSENT_VERSION)
             .apply();
    }

    private void proceedToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    public static boolean hasConsent(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_CONSENT, android.content.Context.MODE_PRIVATE);
        return prefs.getInt(KEY_CONSENT_VERSION, 0) >= CURRENT_CONSENT_VERSION;
    }
}
