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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sosMessenger = new SosMessenger(requireContext());

        // Top App Bar
        ImageButton btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (requireActivity() instanceof com.dweenmd.womensafety.ui.MainActivity) {
                    ((com.dweenmd.womensafety.ui.MainActivity) requireActivity()).openDrawer();
                }
            });
        }
        
        ImageButton btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show());
        }

        // SOS Button
        MaterialButton btnSos = view.findViewById(R.id.btn_sos);
        if (btnSos != null) {
            btnSos.setOnClickListener(v -> triggerSos());
        }
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
