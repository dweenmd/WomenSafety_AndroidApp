package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;

public class PrivacySettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_privacy);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupMenuItem(findViewById(R.id.menu_profile_visibility), android.R.drawable.ic_menu_view, "Profile Visibility", "Who can see my profile", v -> showToast("Profile Visibility"));
        setupMenuItem(findViewById(R.id.menu_phone_visibility), android.R.drawable.stat_sys_phone_call, "Phone Number Visibility", "Who can see my phone number", v -> showToast("Phone Visibility"));
        setupMenuItem(findViewById(R.id.menu_email_visibility), android.R.drawable.ic_dialog_email, "Email Visibility", "Who can see my email", v -> showToast("Email Visibility"));
        
        setupMenuItem(findViewById(R.id.menu_location_sharing_privacy), android.R.drawable.ic_menu_mylocation, "Location Sharing", "Manage location access", v -> showToast("Location Sharing"));
        setupMenuItem(findViewById(R.id.menu_contact_access), android.R.drawable.ic_menu_recent_history, "Contact Access", "Manage contact access", v -> showToast("Contact Access"));
        setupMenuItem(findViewById(R.id.menu_notification_privacy), android.R.drawable.ic_popup_reminder, "Notification Privacy", "Manage notification details", v -> showToast("Notification Privacy"));
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
