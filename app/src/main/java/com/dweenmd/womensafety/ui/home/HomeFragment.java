package com.dweenmd.womensafety.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SosMessenger sosMessenger;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private boolean isHolding = false;

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

        longPressRunnable = () -> {
            if (isHolding) {
                triggerSos();
                isHolding = false; // Prevent multiple triggers from one hold
            }
        };

        btnSos.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    btnSos.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
                    longPressHandler.postDelayed(longPressRunnable, 2000); // 2 second hold
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    btnSos.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    longPressHandler.removeCallbacks(longPressRunnable);
                    return true;
            }
            return false;
        });

        viewModel.getIsServiceRunning().observe(getViewLifecycleOwner(), isRunning -> {
            // Update status card UI (placeholder logic for now)
        });
    }

    private void triggerSos() {
        Toast.makeText(requireContext(), "Triggering SOS...", Toast.LENGTH_SHORT).show();
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
