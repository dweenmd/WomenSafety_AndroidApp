package com.dweenmd.womensafety.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dweenmd.womensafety.R;

/**
 * Shared boilerplate for the standalone menu screens: toolbar with back
 * navigation and the standard three-line menu item wiring. Previously
 * copy-pasted (with slight variations) across the profile activities.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(int toolbarId) {
        Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    protected void setupMenuItem(View menuItem, int iconRes, String title, String subtitle, Runnable onClick) {
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

        menuItem.setOnClickListener(v -> onClick.run());
    }
}
