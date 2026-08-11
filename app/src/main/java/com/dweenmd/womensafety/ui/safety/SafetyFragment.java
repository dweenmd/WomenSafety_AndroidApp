package com.dweenmd.womensafety.ui.safety;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.dweenmd.womensafety.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SafetyFragment extends Fragment {

    private SafetyViewModel viewModel;
    private SharedPreferences prefs;
    private SosMessenger sosMessenger;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private boolean isHolding = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_safety, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SafetyViewModel.class);
        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        sosMessenger = new SosMessenger(requireContext());
        longPressHandler = new Handler(Looper.getMainLooper());

        setupToolbar(view);
        setupStatusCard(view);
        setupSosButton(view);
        setupQuickActions(view);
        setupSwitches(view);
    }

    private void setupToolbar(View view) {
        ImageButton btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).openDrawer();
                }
            });
        }
    }

    private void setupStatusCard(View view) {
        boolean isRunning = isServiceRunning(com.dweenmd.womensafety.service.SosForegroundService.class);
        
        TextView tvTitle = view.findViewById(R.id.tv_protection_title);
        TextView tvDesc = view.findViewById(R.id.tv_protection_desc);
        View iconBg = view.findViewById(R.id.icon_status_bg);

        if (isRunning) {
            tvTitle.setText("Protection Active");
            tvDesc.setText("You are currently protected");
            iconBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.m3_primary_container, null)));
        } else {
            tvTitle.setText("Protection Paused");
            tvDesc.setText("Enable background protection below");
            iconBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.m3_surface_container, null)));
        }
    }

    private void setupSosButton(View view) {
        MaterialButton btnSos = view.findViewById(R.id.btn_sos_main);
        View pulseBg = view.findViewById(R.id.pulse_bg);

        if (pulseBg != null) {
            Animation pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse);
            pulseBg.startAnimation(pulse);
        }

        longPressRunnable = () -> {
            if (isHolding) {
                triggerSos();
                isHolding = false;
            }
        };

        if (btnSos != null) {
            btnSos.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isHolding = true;
                        btnSos.animate().scaleX(0.92f).scaleY(0.92f).setDuration(150).start();
                        longPressHandler.postDelayed(longPressRunnable, 1500);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isHolding = false;
                        btnSos.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        longPressHandler.removeCallbacks(longPressRunnable);
                        if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                        return true;
                }
                return false;
            });
            
            btnSos.setOnClickListener(v -> Toast.makeText(requireContext(), "Hold to SOS", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupQuickActions(View view) {
        view.findViewById(R.id.btn_share_location).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Sharing location...", Toast.LENGTH_SHORT).show();
            sosMessenger.shareLocationOnly(new SosMessenger.SosCallback() {
                @Override
                public void onSosTriggered(String status) {
                    Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                }
            });
        });

        view.findViewById(R.id.btn_manage_contacts).setOnClickListener(v -> 
                NavHostFragment.findNavController(this).navigate(R.id.contactsFragment));
        
        view.findViewById(R.id.btn_check_in).setOnClickListener(v -> 
                Toast.makeText(requireContext(), "Safety Check-in completed", Toast.LENGTH_SHORT).show());
                
        view.findViewById(R.id.btn_safety_timer).setOnClickListener(v -> 
                Toast.makeText(requireContext(), "Safety Timer coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupSwitches(View view) {
        MaterialSwitch switchBackground = view.findViewById(R.id.switch_background_protection);
        MaterialSwitch switchShake = view.findViewById(R.id.switch_shake_detection);
        MaterialSwitch switchLiveLoc = view.findViewById(R.id.switch_live_location);

        if (switchBackground != null) {
            switchBackground.setChecked(prefs.getBoolean("backgroundProtection", true));
            switchBackground.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("backgroundProtection", isChecked).apply();
                if (isChecked) {
                    startSafetyService();
                } else {
                    stopSafetyService();
                }
                setupStatusCard(view);
            });
        }

        if (switchShake != null) {
            switchShake.setChecked(prefs.getBoolean("shakeDetection", true));
            switchShake.setOnCheckedChangeListener((btn, isChecked) -> 
                    prefs.edit().putBoolean("shakeDetection", isChecked).apply());
        }

        if (switchLiveLoc != null) {
            switchLiveLoc.setChecked(prefs.getBoolean("liveLocationOnSos", true));
            switchLiveLoc.setOnCheckedChangeListener((btn, isChecked) -> 
                    prefs.edit().putBoolean("liveLocationOnSos", isChecked).apply());
        }
    }

    private void startSafetyService() {
        Intent intent = new Intent(requireContext(), com.dweenmd.womensafety.service.SosForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
    }

    private void stopSafetyService() {
        Intent intent = new Intent(requireContext(), com.dweenmd.womensafety.service.SosForegroundService.class);
        intent.setAction("stop");
        requireContext().startService(intent);
    }

    private void triggerSos() {
        if (!isAdded()) return;
        sosMessenger.triggerSos(new SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                if (isAdded()) Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        android.app.ActivityManager manager = (android.app.ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
