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

        // Switches removed for new dashboard design

        android.widget.ImageButton btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (requireActivity() instanceof com.dweenmd.womensafety.ui.MainActivity) {
                    ((com.dweenmd.womensafety.ui.MainActivity) requireActivity()).openDrawer();
                }
            });
        }
        
        com.google.android.material.button.MaterialButton btnSos = view.findViewById(R.id.btn_sos_main);
        if (btnSos != null) {
            btnSos.setOnClickListener(v -> {
                android.widget.Toast.makeText(requireContext(), R.string.toast_triggering_sos, android.widget.Toast.LENGTH_SHORT).show();
                new com.dweenmd.womensafety.sos.SosMessenger(requireContext()).triggerSos(new com.dweenmd.womensafety.sos.SosMessenger.SosCallback() {
                    @Override
                    public void onSosTriggered(String status) {
                        android.widget.Toast.makeText(requireContext(), status, android.widget.Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        // Observe ViewModel
        // ViewModel updates for switches removed

    }
}
