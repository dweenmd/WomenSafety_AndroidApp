package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar(R.id.toolbar_settings);

        setupMenuItem(findViewById(R.id.menu_notifications), R.drawable.ic_notifications, "Notifications", "Manage app alerts", this::openNotificationSettings);
        setupMenuItem(findViewById(R.id.menu_appearance), R.drawable.ic_tune, "Appearance", "Light/Dark mode", () -> SettingsDialogs.showAppearanceDialog(this));
        setupMenuItem(findViewById(R.id.menu_language), R.drawable.ic_language, "Language", "Change app language", () -> SettingsDialogs.showLanguageDialog(this));
        setupMenuItem(findViewById(R.id.menu_help), R.drawable.ic_help, "Help & Support", "Get help and send feedback", this::showHelpDialog);
        setupMenuItem(findViewById(R.id.menu_about), R.drawable.ic_info, "About", "Version 1.0.0", this::showAboutDialog);
    }

    private void openNotificationSettings() {
        android.content.Intent intent = new android.content.Intent();
        intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Help & Support")
                .setMessage("If you are in an emergency, press the SOS button to alert your contacts.\n\nFor app support, please contact us at support@womensafety.com")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("About Women Safety")
                .setMessage("Women Safety App\nVersion 1.0.0\n\nDeveloped to empower and protect women through technology.")
                .setPositiveButton("Close", null)
                .show();
    }
}
