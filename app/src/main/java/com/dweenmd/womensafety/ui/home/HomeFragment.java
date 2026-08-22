package com.dweenmd.womensafety.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.service.SosForegroundService;
import com.dweenmd.womensafety.sos.SosButtonController;
import com.dweenmd.womensafety.sos.SosMessenger;
import com.dweenmd.womensafety.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeFragment extends Fragment {

    private SosMessenger sosMessenger;
    private ContactsRepository contactsRepository;
    private SosButtonController sosButtonController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sosMessenger = new SosMessenger(requireContext());
        contactsRepository = new ContactsRepository(requireContext());

        setupToolbar(view);
        setupGreeting(view);
        setupStatusCard(view);
        setupSosButton(view);
        setupQuickActions(view);
        setupEmergencyContacts(view);
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

        ImageButton btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> Toast.makeText(requireContext(), "No new safety alerts", Toast.LENGTH_SHORT).show());
        }

        ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && ivAvatar != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(ivAvatar);
            }
        }
    }

    private void setupGreeting(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName().split(" ")[0] : "Safe Guardian";
            tvGreeting.setText("Hello, " + name);
        }
    }

    private void setupStatusCard(View view) {
        boolean isRunning = SosForegroundService.isProtectionRunning(requireContext());
        
        TextView tvTitle = view.findViewById(R.id.tv_status_title);
        TextView tvDesc = view.findViewById(R.id.tv_status_desc);
        View indicator = view.findViewById(R.id.view_status_indicator);
        View card = view.findViewById(R.id.card_status_home);

        if (isRunning) {
            tvTitle.setText("You're Protected");
            tvDesc.setText("Background protection is active");
            indicator.setBackgroundResource(R.drawable.green_clr);
        } else {
            tvTitle.setText("Protection Off");
            tvDesc.setText("Tap to enable safety features");
            indicator.setBackgroundResource(R.drawable.rounded_bg_gray); // Assume gray for inactive
        }

        if (card != null) {
            card.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.safetyFragment));
        }
    }

    private void setupSosButton(View view) {
        MaterialButton btnSos = view.findViewById(R.id.btn_sos);
        View pulseBg = view.findViewById(R.id.pulse_bg);

        if (btnSos != null) {
            sosButtonController = new SosButtonController(
                    btnSos,
                    pulseBg,
                    this::triggerSos,
                    v -> Toast.makeText(requireContext(), "Hold for 1.5 seconds to SOS", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupQuickActions(View view) {
        View btnShareLoc = view.findViewById(R.id.btn_share_location_home);
        if (btnShareLoc != null) {
            btnShareLoc.setOnClickListener(v ->
                    com.dweenmd.womensafety.sos.LiveShareUi.handle(HomeFragment.this));
        }

        View btnTimer = view.findViewById(R.id.btn_safety_timer_home);
        if (btnTimer != null) {
            btnTimer.setOnClickListener(v -> Toast.makeText(requireContext(), "Safety Timer coming soon!", Toast.LENGTH_SHORT).show());
        }

        View btnFakeCall = view.findViewById(R.id.btn_fake_call_home);
        if (btnFakeCall != null) {
            btnFakeCall.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), com.dweenmd.womensafety.ui.features.FakeCallActivity.class)));
        }
    }

    private void setupEmergencyContacts(View view) {
        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        
        TextView tvName = view.findViewById(R.id.tv_contact_name_home);
        TextView tvRelation = view.findViewById(R.id.tv_contact_relation_home);
        ImageView ivAvatar = view.findViewById(R.id.iv_contact_avatar_home);
        View card = view.findViewById(R.id.card_primary_contact_home);
        View btnCall = view.findViewById(R.id.btn_contact_call_home);

        if (!contacts.isEmpty()) {
            ContactsRepository.Contact primary = contacts.get(0); // Repository sorts primary first
            tvName.setText(primary.name);
            tvRelation.setText(primary.relationship);
            
            if (btnCall != null) {
                btnCall.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + primary.phone));
                    startActivity(intent);
                });
            }
        }

        if (card != null) {
            card.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.contactsFragment));
        }
    }

    private void triggerSos() {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), "🚨 SOS TRIGGERED! 🚨", Toast.LENGTH_SHORT).show();
        sosMessenger.triggerSos(new SosMessenger.SosCallback() {
            @Override
            public void onSosTriggered(String status) {
                if (isAdded()) Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
