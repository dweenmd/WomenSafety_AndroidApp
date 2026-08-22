package com.dweenmd.womensafety.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;

public class PermissionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        setupToolbar(R.id.toolbar_permissions);

        updateUI();

        findViewById(R.id.btn_open_system_settings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        setupPermissionItem(findViewById(R.id.perm_location), R.drawable.ic_location, 
                "Location", "Access live GPS for SOS sharing", Manifest.permission.ACCESS_FINE_LOCATION);
        
        setupPermissionItem(findViewById(R.id.perm_sms), R.drawable.ic_sos,
                "SMS", "Send emergency messages to contacts", Manifest.permission.SEND_SMS);

        setupPermissionItem(findViewById(R.id.perm_phone), R.drawable.ic_phone,
                "Phone", "Instantly dial emergency services", Manifest.permission.CALL_PHONE);
        
        setupPermissionItem(findViewById(R.id.perm_contacts), R.drawable.ic_contacts, 
                "Contacts", "Import trusted contacts from phone", Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setupPermissionItem(findViewById(R.id.perm_notifications), R.drawable.ic_notifications, 
                    "Notifications", "Show active protection status", Manifest.permission.POST_NOTIFICATIONS);
        } else {
            findViewById(R.id.perm_notifications).setVisibility(View.GONE);
        }
    }

    private void setupPermissionItem(View itemView, int iconRes, String title, String description, String permission) {
        ImageView ivIcon = itemView.findViewById(R.id.iv_perm_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_perm_title);
        TextView tvDesc = itemView.findViewById(R.id.tv_perm_description);
        ImageView ivStatus = itemView.findViewById(R.id.iv_perm_status);

        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvDesc.setText(description);

        boolean isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        
        if (isGranted) {
            ivStatus.setImageResource(R.drawable.ic_check);
            ivStatus.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_safe_green)));
        } else {
            ivStatus.setImageResource(R.drawable.ic_warning);
            ivStatus.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.m3_error)));
        }

        itemView.setOnClickListener(v -> {
            if (!isGranted) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, 100);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updateUI();
    }
}
