package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;

public class PrivacySettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        setupToolbar(R.id.toolbar_privacy);

        setupMenuItem(findViewById(R.id.menu_profile_visibility), R.drawable.ic_eye, "Profile Visibility", "Who can see my profile", () -> showToast("Profile Visibility"));
        setupMenuItem(findViewById(R.id.menu_phone_visibility), R.drawable.ic_phone, "Phone Number Visibility", "Who can see my phone number", () -> showToast("Phone Visibility"));
        setupMenuItem(findViewById(R.id.menu_email_visibility), R.drawable.ic_email_elegant, "Email Visibility", "Who can see my email", () -> showToast("Email Visibility"));

        setupMenuItem(findViewById(R.id.menu_location_sharing_privacy), R.drawable.ic_location, "Location Sharing", "Manage location access", () -> showToast("Location Sharing"));
        setupMenuItem(findViewById(R.id.menu_contact_access), R.drawable.ic_history, "Contact Access", "Manage contact access", () -> showToast("Contact Access"));
        setupMenuItem(findViewById(R.id.menu_notification_privacy), R.drawable.ic_notifications, "Notification Privacy", "Manage notification details", () -> showToast("Notification Privacy"));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
