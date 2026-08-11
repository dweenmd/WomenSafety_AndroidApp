package com.dweenmd.womensafety.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.LocaleListCompat;

import com.dweenmd.womensafety.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AppPreferencesActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preferences);

        prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar_preferences);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupMenuItem(findViewById(R.id.pref_theme), android.R.drawable.ic_menu_gallery, 
                "Theme", "Choose light or dark mode", this::showAppearanceDialog);
        
        setupMenuItem(findViewById(R.id.pref_language), android.R.drawable.ic_menu_sort_alphabetically, 
                "Language", "Select your preferred language", this::showLanguageDialog);
    }

    private void setupMenuItem(View view, int iconRes, String title, String subtitle, Runnable onClick) {
        ImageView icon = view.findViewById(R.id.iv_menu_icon);
        TextView tvTitle = view.findViewById(R.id.tv_menu_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_menu_subtitle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);

        view.setOnClickListener(v -> onClick.run());
    }

    private void showAppearanceDialog() {
        String[] options = {"System Default", "Light", "Dark"};
        int currentTheme = prefs.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        int checkedItem = 0;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 1;
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 2;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (which == 1) mode = AppCompatDelegate.MODE_NIGHT_NO;
                    else if (which == 2) mode = AppCompatDelegate.MODE_NIGHT_YES;
                    
                    AppCompatDelegate.setDefaultNightMode(mode);
                    prefs.edit().putInt("themeMode", mode).apply();
                    dialog.dismiss();
                })
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "বাংলা", "हिंदी"};
        String[] codes = {"en", "bn", "hi"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setItems(languages, (dialog, which) -> {
                String selectedCode = codes[which];
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(selectedCode)
                );
            })
            .show();
    }
}
