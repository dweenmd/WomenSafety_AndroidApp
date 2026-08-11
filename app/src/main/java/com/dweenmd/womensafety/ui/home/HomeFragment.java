package com.dweenmd.womensafety.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SosMessenger sosMessenger;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private boolean isHolding = false;
    private AnimatorSet pulseAnimation;
    private AnimatorSet idlePulseAnimation;
    private boolean isServiceRunningState = false;

    private TextView tvStatusTitle, tvStatusDesc;
    private ImageView ivStatusIcon;
    private View pulse1, pulse2;
    private View cardStatus;
    private MaterialSwitch switchProtection;
    private TextView tvContactSummary, tvManageContacts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sosMessenger = new SosMessenger(requireContext());
        longPressHandler = new Handler(Looper.getMainLooper());

        MaterialButton btnSos = view.findViewById(R.id.btn_sos);
        pulse1 = view.findViewById(R.id.pulse_bg_1);
        pulse2 = view.findViewById(R.id.pulse_bg_2);
        
        cardStatus = view.findViewById(R.id.card_status);
        tvStatusTitle = view.findViewById(R.id.tv_status_title);
        tvStatusDesc = view.findViewById(R.id.tv_status_desc);
        ivStatusIcon = view.findViewById(R.id.iv_status_icon);
        switchProtection = view.findViewById(R.id.switch_protection);
        
        tvContactSummary = view.findViewById(R.id.tv_contact_summary);
        tvManageContacts = view.findViewById(R.id.tv_manage_contacts);

        setupPulseAnimations();
        setupQuickActions(view);
        updateBatteryStatus();
        setupContactsSummary();

        longPressRunnable = () -> {
            if (isHolding) {
                triggerSos();
                isHolding = false; 
                stopPulseAnimation();
                btnSos.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                updateIdlePulseState();
            }
        };

        btnSos.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    stopIdlePulseAnimation();
                    btnSos.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start();
                    startPulseAnimation();
                    longPressHandler.postDelayed(longPressRunnable, 2000); // 2 second hold
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isHolding) { // Only stop if it hasn't triggered
                        isHolding = false;
                        btnSos.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                        stopPulseAnimation();
                        longPressHandler.removeCallbacks(longPressRunnable);
                        updateIdlePulseState();
                    }
                    return true;
            }
            return false;
        });

        viewModel.getIsServiceRunning().observe(getViewLifecycleOwner(), isRunning -> {
            isServiceRunningState = isRunning;
            
            // Sync switch without triggering listener
            switchProtection.setOnCheckedChangeListener(null);
            switchProtection.setChecked(isRunning);
            switchProtection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    toggleProtectionService(isChecked);
                }
            });

            if (isRunning) {
                tvStatusTitle.setText(R.string.home_protection_active);
                tvStatusTitle.setTextColor(getResources().getColor(R.color.status_safe, null));
                ivStatusIcon.setColorFilter(getResources().getColor(R.color.status_safe, null));
                cardStatus.setBackgroundResource(R.drawable.bg_status_active);
            } else {
                tvStatusTitle.setText(R.string.home_protection_inactive);
                tvStatusTitle.setTextColor(getResources().getColor(R.color.status_warning, null));
                ivStatusIcon.setColorFilter(getResources().getColor(R.color.status_warning, null));
                cardStatus.setBackgroundResource(R.drawable.bg_status_inactive);
            }
            
            updateIdlePulseState();
        });
        
        switchProtection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                toggleProtectionService(isChecked);
            }
        });
    }
    
    private void toggleProtectionService(boolean start) {
        Intent serviceIntent = new Intent(requireContext(), com.dweenmd.womensafety.service.SosForegroundService.class);
        if (start) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                requireContext().startForegroundService(serviceIntent);
            } else {
                requireContext().startService(serviceIntent);
            }
        } else {
            requireContext().stopService(serviceIntent);
        }
        // Assuming SafetyViewModel or MainActivity updates the state, or we update it here if needed.
        // Actually SosForegroundService lifecycle should update it or we update it optimistically.
        // For now, let's also update ViewModel optimistically so UI responds immediately.
        viewModel.setServiceRunning(start);
    }
    
    private void setupContactsSummary() {
        ContactsRepository contactsRepo = new ContactsRepository(requireContext());
        contactsRepo.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            int count = contacts != null ? contacts.size() : 0;
            tvContactSummary.setText(getString(R.string.home_contacts_count_ready, count));
        });
        
        tvManageContacts.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.contactsFragment);
            } catch (Exception e) {
                // Ignore if navigation action isn't available
            }
        });
    }

    private void setupQuickActions(View view) {
        ImageButton btnFakeCall = view.findViewById(R.id.btn_quick_fake_call);
        ImageButton btnRecord = view.findViewById(R.id.btn_quick_record);
        ImageButton btnLiveLink = view.findViewById(R.id.btn_quick_share_location);
        
        btnFakeCall.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.toast_fake_call_scheduled, Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(requireContext(), com.dweenmd.womensafety.ui.features.FakeCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }, 5000);
        });
        
        btnRecord.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.toast_started_audio_record, Toast.LENGTH_SHORT).show();
            new com.dweenmd.womensafety.sos.AudioRecorderHelper(requireContext()).startRecording();
        });
        
        btnLiveLink.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.toast_fetching_location, Toast.LENGTH_SHORT).show();
            sosMessenger.shareLocationOnly(new SosMessenger.SosCallback() {
                @Override
                public void onSosTriggered(String status) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailure(String error) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }
    
    private void updateBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = requireContext().registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;
            tvStatusDesc.setText(getString(R.string.home_battery_status, (int)batteryPct));
        }
    }

    private void setupPulseAnimations() {
        // Active SOS Hold Animation
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(pulse1, "scaleX", 1f, 1.8f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(pulse1, "scaleY", 1f, 1.8f);
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(pulse1, "alpha", 0.5f, 0f);
        
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(pulse2, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(pulse2, "scaleY", 1f, 1.5f);
        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(pulse2, "alpha", 0.3f, 0f);

        scaleX2.setStartDelay(500);
        scaleY2.setStartDelay(500);
        alpha2.setStartDelay(500);

        pulseAnimation = new AnimatorSet();
        pulseAnimation.playTogether(scaleX1, scaleY1, alpha1, scaleX2, scaleY2, alpha2);
        pulseAnimation.setDuration(1500);
        pulseAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isHolding) {
                    pulseAnimation.start();
                }
            }
        });
        
        // Idle Pulse Animation
        ObjectAnimator idleScaleX = ObjectAnimator.ofFloat(pulse1, "scaleX", 1f, 1.3f);
        ObjectAnimator idleScaleY = ObjectAnimator.ofFloat(pulse1, "scaleY", 1f, 1.3f);
        ObjectAnimator idleAlpha = ObjectAnimator.ofFloat(pulse1, "alpha", 0.2f, 0f);
        
        idlePulseAnimation = new AnimatorSet();
        idlePulseAnimation.playTogether(idleScaleX, idleScaleY, idleAlpha);
        idlePulseAnimation.setDuration(3000); // Slow pulse
        idlePulseAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        idlePulseAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!isHolding && !isServiceRunningState) {
                    idlePulseAnimation.start(); // loop
                }
            }
        });
    }
    
    private void updateIdlePulseState() {
        if (!isHolding && !isServiceRunningState) {
            startIdlePulseAnimation();
        } else {
            stopIdlePulseAnimation();
        }
    }

    private void startPulseAnimation() {
        pulse1.setVisibility(View.VISIBLE);
        pulse2.setVisibility(View.VISIBLE);
        pulse1.setAlpha(0.5f);
        pulse2.setAlpha(0.3f);
        pulseAnimation.start();
    }

    private void stopPulseAnimation() {
        pulseAnimation.cancel();
        pulse1.setVisibility(View.INVISIBLE);
        pulse2.setVisibility(View.INVISIBLE);
        pulse1.setScaleX(1f); pulse1.setScaleY(1f);
        pulse2.setScaleX(1f); pulse2.setScaleY(1f);
    }
    
    private void startIdlePulseAnimation() {
        if (!idlePulseAnimation.isRunning()) {
            pulse1.setVisibility(View.VISIBLE);
            pulse2.setVisibility(View.INVISIBLE); // Only use pulse1 for idle
            pulse1.setScaleX(1f); pulse1.setScaleY(1f);
            pulse1.setAlpha(0.2f);
            idlePulseAnimation.start();
        }
    }
    
    private void stopIdlePulseAnimation() {
        idlePulseAnimation.cancel();
        pulse1.setVisibility(View.INVISIBLE);
    }

    private void triggerSos() {
        Toast.makeText(requireContext(), R.string.toast_triggering_sos, Toast.LENGTH_SHORT).show();
        sosMessenger.triggerSos(new SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
