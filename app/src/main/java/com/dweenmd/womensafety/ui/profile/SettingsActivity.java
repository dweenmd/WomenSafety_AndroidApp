package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupMenuItem(findViewById(R.id.menu_notifications), android.R.drawable.ic_popup_reminder, "Notifications", "Manage app alerts", this::openNotificationSettings);
        setupMenuItem(findViewById(R.id.menu_appearance), android.R.drawable.ic_menu_gallery, "Appearance", "Light/Dark mode", this::showAppearanceDialog);
        setupMenuItem(findViewById(R.id.menu_language), android.R.drawable.ic_menu_sort_alphabetically, "Language", "Change app language", this::showLanguageDialog);
        setupMenuItem(findViewById(R.id.menu_help), android.R.drawable.ic_menu_help, "Help & Support", "Get help and send feedback", this::showHelpDialog);
        setupMenuItem(findViewById(R.id.menu_about), android.R.drawable.ic_menu_info_details, "About", "Version 1.0.0", this::showAboutDialog);
    }

    private void setupMenuItem(android.view.View view, int iconRes, String title, String subtitle, Runnable onClick) {
        ImageView icon = view.findViewById(R.id.iv_menu_icon);
        TextView tvTitle = view.findViewById(R.id.tv_menu_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_menu_subtitle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);

        view.setOnClickListener(v -> onClick.run());
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

    private void showAppearanceDialog() {
        String[] options = {"System Default", "Light", "Dark"};
        
        android.content.SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        int currentTheme = prefs.getInt("themeMode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        int checkedItem = 0;
        if (currentTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 1;
        else if (currentTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 2;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (which == 1) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                    else if (which == 2) mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                    
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
                    prefs.edit().putInt("themeMode", mode).apply();
                    dialog.dismiss();
                })
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "বাংলা", "हिंदी", "Español", "Français", "العربية", "Português"};
        String[] codes = {"en", "bn", "hi", "es", "fr", "ar", "pt"};
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setItems(languages, (dialog, which) -> {
                String selectedCode = codes[which];
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(selectedCode)
                );
            })
            .show();
    }

    private void showHelpDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Help & Support")
                .setMessage("If you are in an emergency, press the SOS button to alert your contacts.\n\nFor app support, please contact us at support@womensafety.com")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("About Women Safety")
                .setMessage("Women Safety App\nVersion 1.0.0\n\nDeveloped to empower and protect women through technology.")
                .setPositiveButton("Close", null)
                .show();
    }
}
