package com.dweenmd.womensafety.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.dweenmd.womensafety.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Edge-to-edge support for BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Set up Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
        
        checkAndRequestPermissions();
        checkAndStartService();
    }
    
    private void checkAndRequestPermissions() {
        java.util.List<String> permissions = new java.util.ArrayList<>();
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        String[] requiredPermissions = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        for (String perm : requiredPermissions) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, perm) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(perm);
            }
        }
        
        if (!permissions.isEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_NOTIFICATION_PERMISSION);
        }
    }
    
    private void checkAndStartService() {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        boolean isProtectionEnabled = prefs.getBoolean("backgroundProtection", true);
        
        if (isProtectionEnabled) {
            android.content.Intent serviceIntent = new android.content.Intent(this, com.dweenmd.womensafety.service.SosForegroundService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }
}
