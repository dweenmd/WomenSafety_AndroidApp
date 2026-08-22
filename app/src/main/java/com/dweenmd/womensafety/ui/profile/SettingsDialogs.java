package com.dweenmd.womensafety.ui.profile;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Single source of truth for the theme and language pickers, previously
 * duplicated (with diverging language lists) in SettingsActivity and
 * AppPreferencesActivity.
 */
final class SettingsDialogs {

    private SettingsDialogs() {}

    static void showAppearanceDialog(Activity activity) {
        String[] options = {"System Default", "Light", "Dark"};

        android.content.SharedPreferences prefs =
                activity.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        int currentTheme = prefs.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int checkedItem = 0;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 1;
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 2;

        new MaterialAlertDialogBuilder(activity)
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

    static void showLanguageDialog(Activity activity) {
        // Kept in sync with the translated locale folders (values-bn/hi/es/fr/ar/pt).
        String[] languages = {"English", "বাংলা", "हिंदी", "Español", "Français", "العربية", "Português"};
        String[] codes = {"en", "bn", "hi", "es", "fr", "ar", "pt"};

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(codes[which])))
                .show();
    }
}
