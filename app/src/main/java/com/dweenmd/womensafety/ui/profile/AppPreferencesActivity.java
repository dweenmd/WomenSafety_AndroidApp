package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;

public class AppPreferencesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preferences);

        setupToolbar(R.id.toolbar_preferences);

        setupMenuItem(findViewById(R.id.pref_theme), R.drawable.ic_tune,
                "Theme", "Choose light or dark mode", () -> SettingsDialogs.showAppearanceDialog(this));

        setupMenuItem(findViewById(R.id.pref_language), R.drawable.ic_language,
                "Language", "Select your preferred language", () -> SettingsDialogs.showLanguageDialog(this));
    }
}
