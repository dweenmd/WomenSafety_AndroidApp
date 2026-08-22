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

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.imageview.ShapeableImageView;
import com.bumptech.glide.Glide;

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
            
            com.google.android.material.navigation.NavigationView navView = findViewById(R.id.nav_view);
            NavigationUI.setupWithNavController(navView, navController);

            // Handle custom clicks in Navigation Drawer
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                boolean handled;
                
                if (id == R.id.nav_settings) {
                    startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.profile.SettingsActivity.class));
                    handled = true;
                } else if (id == R.id.nav_signout) {
                    new com.dweenmd.womensafety.data.AuthRepository(this).signOut();
                    startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.auth.LoginActivity.class));
                    finish();
                    handled = true;
                } else if (id == R.id.nav_emergency_sos) {
                    confirmTriggerSos();
                    handled = true;
                } else if (id == R.id.nav_fake_call) {
                    startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.features.FakeCallActivity.class));
                    handled = true;
                } else if (id == R.id.nav_live_location) {
                    navController.navigate(R.id.safetyFragment);
                    handled = true;
                } else if (id == R.id.nav_activity_log) {
                    startActivity(new android.content.Intent(this, com.dweenmd.womensafety.ui.profile.LoginActivityHistoryActivity.class));
                    handled = true;
                } else if (id == R.id.nav_about) {
                    showAboutDialog();
                    handled = true;
                } else {
                    // Let NavigationUI handle standard fragment navigation
                    handled = NavigationUI.onNavDestinationSelected(item, navController);
                }
                
                if (handled) {
                    androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    if (drawer != null) {
                        drawer.closeDrawer(androidx.core.view.GravityCompat.START);
                    }
                }
                return handled;
            });

            setupNavHeader(navView, navController);

            // Make the "Sign Out" item red to match the design
            android.view.Menu menu = navView.getMenu();
            android.view.MenuItem logoutItem = menu.findItem(R.id.nav_signout);
            if (logoutItem != null) {
                android.text.SpannableString s = new android.text.SpannableString(logoutItem.getTitle());
                s.setSpan(new android.text.style.ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(this, R.color.m3_error)), 0, s.length(), 0);
                logoutItem.setTitle(s);
            }
        }
        
        checkAndRequestPermissions();
        checkAndStartService();
    }

    @Override
    public void onBackPressed() {
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawer.closeDrawer(androidx.core.view.GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupNavHeader(com.google.android.material.navigation.NavigationView navView, NavController navController) {
        View headerView = navView.getHeaderView(0);
        if (headerView == null) return;

        TextView nameText = headerView.findViewById(R.id.drawer_name);
        TextView emailText = headerView.findViewById(R.id.drawer_email);
        ShapeableImageView avatarImage = headerView.findViewById(R.id.drawer_avatar);
        android.widget.Button viewProfileBtn = headerView.findViewById(R.id.drawer_view_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            nameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            emailText.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(avatarImage);
            }
        }

        viewProfileBtn.setOnClickListener(v -> {
            navController.navigate(R.id.profileFragment);
            androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer != null) {
                drawer.closeDrawer(androidx.core.view.GravityCompat.START);
            }
        });
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About WomenSafety")
                .setMessage("WomenSafety is your personal safety companion. " +
                        "\n\nVersion: 1.0" +
                        "\nDeveloped with ❤️ for safety.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmTriggerSos() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.drawer_sos_confirm_title)
                .setMessage(R.string.drawer_sos_confirm_message)
                .setPositiveButton(R.string.drawer_menu_emergency_sos, (dialog, which) -> triggerSosFromDrawer())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void triggerSosFromDrawer() {
        com.dweenmd.womensafety.sos.SosMessenger messenger = new com.dweenmd.womensafety.sos.SosMessenger(this);
        Toast.makeText(this, "🚨 SOS TRIGGERED! 🚨", Toast.LENGTH_SHORT).show();
        messenger.triggerSos(new com.dweenmd.womensafety.sos.SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                Toast.makeText(MainActivity.this, status, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "SOS failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public void openDrawer() {
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.openDrawer(androidx.core.view.GravityCompat.START);
        }
    }
    
    private void checkAndRequestPermissions() {
        java.util.List<String> permissions = new java.util.ArrayList<>();
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        String[] requiredPermissions = {
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE
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
            // Check for location permission before starting location FGS to prevent Android 14 crashes
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.content.Intent serviceIntent = new android.content.Intent(this, com.dweenmd.womensafety.service.SosForegroundService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            checkAndStartService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // BootReceiver can't start the location FGS directly on Android 14+,
        // so it sets a flag and we restart protection once the app is in the foreground.
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        if (prefs.getBoolean(com.dweenmd.womensafety.service.SosForegroundService.KEY_RESTART_PENDING, false)) {
            prefs.edit().putBoolean(com.dweenmd.womensafety.service.SosForegroundService.KEY_RESTART_PENDING, false).apply();
            checkAndStartService();
        }
    }
}
