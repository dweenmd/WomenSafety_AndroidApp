package com.dweenmd.womensafety.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvEmail = view.findViewById(R.id.tv_profile_email);
        TextView btnLogout = view.findViewById(R.id.btn_logout);
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        viewModel.getName().observe(getViewLifecycleOwner(), tvName::setText);
        viewModel.getEmail().observe(getViewLifecycleOwner(), tvEmail::setText);

        btnEditProfile.setOnClickListener(v -> showToast("Edit Profile coming soon"));

        // ACCOUNT
        setupMenuItem(view, R.id.menu_personal_info, android.R.drawable.ic_menu_myplaces, "Personal Information", "Name, phone and email");
        setupMenuItem(view, R.id.menu_security, android.R.drawable.ic_lock_idle_lock, "Password & Security", "Manage password and account security");
        setupMenuItem(view, R.id.menu_privacy, android.R.drawable.ic_secure, "Privacy", "Control your privacy settings");

        // SAFETY
        setupMenuItem(view, R.id.menu_emergency_contacts, android.R.drawable.ic_menu_my_calendar, "Emergency Contacts", "Manage people who can help in an emergency");
        setupMenuItem(view, R.id.menu_sos_settings, android.R.drawable.ic_dialog_alert, "SOS Settings", "Configure emergency actions");
        setupMenuItem(view, R.id.menu_location, android.R.drawable.ic_menu_mylocation, "Location Sharing", "Manage emergency location sharing");
        setupMenuItem(view, R.id.menu_safety_notifications, android.R.drawable.ic_popup_reminder, "Safety Notifications", "Manage important safety alerts");

        // APP
        setupMenuItem(view, R.id.menu_notifications, android.R.drawable.ic_menu_info_details, "Notifications", "App notification preferences");
        setupMenuItem(view, R.id.menu_appearance, android.R.drawable.ic_menu_view, "Appearance", "Light/Dark mode and themes");
        setupMenuItem(view, R.id.menu_language, android.R.drawable.ic_menu_sort_alphabetically, "Language", "Change app language");
        setupMenuItem(view, R.id.menu_help, android.R.drawable.ic_menu_help, "Help & Support", "Get help or contact us");
        setupMenuItem(view, R.id.menu_about, android.R.drawable.ic_menu_info_details, "About", "App version and information");

        // Navigation for Emergency Contacts
        view.findViewById(R.id.menu_emergency_contacts).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.contactsFragment);
        });

        btnLogout.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        new AuthRepository(requireContext()).signOut();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupMenuItem(View parent, int includeId, int iconRes, String title, String subtitle) {
        View menuItem = parent.findViewById(includeId);
        if (menuItem != null) {
            ImageView ivIcon = menuItem.findViewById(R.id.iv_menu_icon);
            TextView tvTitle = menuItem.findViewById(R.id.tv_menu_title);
            TextView tvSubtitle = menuItem.findViewById(R.id.tv_menu_subtitle);

            if (ivIcon != null) ivIcon.setImageResource(iconRes);
            if (tvTitle != null) tvTitle.setText(title);
            if (tvSubtitle != null) tvSubtitle.setText(subtitle);

            // Default click listener for unimplemented features
            menuItem.setOnClickListener(v -> showToast(title + " coming soon"));
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
