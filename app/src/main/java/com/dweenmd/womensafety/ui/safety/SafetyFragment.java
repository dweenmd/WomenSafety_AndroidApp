package com.dweenmd.womensafety.ui.safety;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.ui.auth.LoginActivity;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SafetyFragment extends Fragment {

    private SafetyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_safety, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SafetyViewModel.class);

        MaterialSwitch switchBackground = view.findViewById(R.id.switch_background_protection);
        MaterialSwitch switchLiveLocation = view.findViewById(R.id.switch_live_location);
        MaterialSwitch switchShake = view.findViewById(R.id.switch_shake_detection);
        MaterialSwitch switchAutoNotify = view.findViewById(R.id.switch_auto_notify);

        // Observe ViewModel
        viewModel.getBackgroundProtection().observe(getViewLifecycleOwner(), switchBackground::setChecked);
        viewModel.getLiveLocationOnSos().observe(getViewLifecycleOwner(), switchLiveLocation::setChecked);
        viewModel.getShakeDetection().observe(getViewLifecycleOwner(), switchShake::setChecked);
        viewModel.getAutoNotify().observe(getViewLifecycleOwner(), switchAutoNotify::setChecked);

        // Update ViewModel on change
        switchBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) viewModel.setSetting("backgroundProtection", isChecked);
        });
        switchLiveLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) viewModel.setSetting("liveLocationOnSos", isChecked);
        });
        switchShake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) viewModel.setSetting("shakeDetection", isChecked);
        });
        switchAutoNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) viewModel.setSetting("autoNotify", isChecked);
        });

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AuthRepository(requireContext(), "YOUR_WEB_CLIENT_ID").signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}
