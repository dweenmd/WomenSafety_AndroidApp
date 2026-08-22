package com.dweenmd.womensafety.ui.safety;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.service.SosForegroundService;
import com.dweenmd.womensafety.sos.SosButtonController;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.dweenmd.womensafety.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SafetyFragment extends Fragment {

    private SafetyViewModel viewModel;
    private SharedPreferences prefs;
    private SosMessenger sosMessenger;
    private SosButtonController sosButtonController;

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

        setupToolbar(view);
        setupStatusCard(view);
        setupSosButton(view);
        setupQuickActions(view);
        setupSwitches(view);
    }

    @Override
    public void onDestroyView() {
        if (sosButtonController != null) {
            sosButtonController.destroy();
            sosButtonController = null;
        }
        super.onDestroyView();
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
        boolean isRunning = SosForegroundService.isProtectionRunning(requireContext());
        
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

        if (btnSos != null) {
            sosButtonController = new SosButtonController(
                    btnSos,
                    pulseBg,
                    this::triggerSos,
                    v -> Toast.makeText(requireContext(), "Hold to SOS", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupQuickActions(View view) {
        view.findViewById(R.id.btn_emergency_call).setOnClickListener(v -> sosMessenger.dialEmergencyNumber());

        view.findViewById(R.id.btn_share_location).setOnClickListener(v ->
                com.dweenmd.womensafety.sos.LiveShareUi.handle(SafetyFragment.this));
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

        setupSimSelector(view);
    }

    private void setupSimSelector(View view) {
        TextView tvSimValue = view.findViewById(R.id.tv_sms_sim_value);
        View rowSim = view.findViewById(R.id.row_sms_sim);
        if (tvSimValue == null || rowSim == null) return;

        tvSimValue.setText(com.dweenmd.womensafety.sos.SmsSimManager.getPreferredSimLabel(requireContext()));

        rowSim.setOnClickListener(v -> {
            java.util.List<com.dweenmd.womensafety.sos.SmsSimManager.SimInfo> sims =
                    com.dweenmd.womensafety.sos.SmsSimManager.getActiveSims(requireContext());

            if (sims.isEmpty()) {
                // Single SIM or no permission — offer to request permission when needed
                if (!com.dweenmd.womensafety.sos.SmsSimManager.hasPhoneStatePermission(requireContext())) {
                    androidx.core.app.ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{android.Manifest.permission.READ_PHONE_STATE}, 200);
                    Toast.makeText(requireContext(), "Grant Phone permission to pick a SIM", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Only one SIM is active on this device", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            long current = com.dweenmd.womensafety.sos.SmsSimManager.getPreferredSubId(requireContext());
            java.util.List<String> labels = new java.util.ArrayList<>();
            labels.add(getString(R.string.safety_sim_default));
            for (com.dweenmd.womensafety.sos.SmsSimManager.SimInfo sim : sims) {
                labels.add(sim.label());
            }
            int checked = 0;
            for (int i = 0; i < sims.size(); i++) {
                if (sims.get(i).subId == current) {
                    checked = i + 1;
                    break;
                }
            }

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.safety_sim_select_title)
                    .setSingleChoiceItems(labels.toArray(new String[0]), checked, (dialog, which) -> {
                        long subId = (which == 0) ? -1L : sims.get(which - 1).subId;
                        com.dweenmd.womensafety.sos.SmsSimManager.setPreferredSubId(requireContext(), subId);
                        tvSimValue.setText(com.dweenmd.womensafety.sos.SmsSimManager.getPreferredSimLabel(requireContext()));
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    private void startSafetyService() {
        Intent intent = new Intent(requireContext(), SosForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
    }

    private void stopSafetyService() {
        // Direct stopService: the old start-with-"stop"-action approach created the
        // service (registering receivers) even when it wasn't running.
        requireContext().stopService(new Intent(requireContext(), SosForegroundService.class));
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
}
