package com.dweenmd.womensafety.ui.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private AuthRepository authRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authRepository = new AuthRepository(requireContext());
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.fetchUserProfile();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Views
        TextView tvName = view.findViewById(R.id.tv_profile_header_name);
        TextView tvEmail = view.findViewById(R.id.tv_profile_header_email);
        TextView tvPhone = view.findViewById(R.id.tv_profile_header_phone);
        ImageView ivAvatar = view.findViewById(R.id.iv_profile_header_avatar);
        View btnEditAvatar = view.findViewById(R.id.btn_edit_avatar_header);
        View btnCompleteProfile = view.findViewById(R.id.btn_complete_profile);
        View cardProfileCompletion = view.findViewById(R.id.card_profile_completion);
        View btnDismissCompletion = view.findViewById(R.id.btn_dismiss_completion);

        // Initialize header data
        viewModel.getName().observe(getViewLifecycleOwner(), tvName::setText);
        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> tvEmail.setText(maskEmail(email)));
        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> {
            if (phone != null && !phone.isEmpty() && !phone.equals("No Phone")) {
                tvPhone.setText(maskPhone(phone));
            } else {
                tvPhone.setText("Phone not set");
            }
        });
        viewModel.getPhotoUrl().observe(getViewLifecycleOwner(), photoUrl -> {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(ivAvatar);
            }
        });

        View.OnClickListener editProfileListener = v -> startActivity(new Intent(requireContext(), EditProfileActivity.class));
        btnEditAvatar.setOnClickListener(editProfileListener);
        btnCompleteProfile.setOnClickListener(editProfileListener);
        
        if (btnDismissCompletion != null && cardProfileCompletion != null) {
            btnDismissCompletion.setOnClickListener(v -> cardProfileCompletion.setVisibility(View.GONE));
        }

        // ACCOUNT
        setupMenuItem(view.findViewById(R.id.menu_personal_info), android.R.drawable.ic_menu_info_details, "Personal Information", "Name, phone & email", R.color.tint_account_bg, R.color.tint_account_icon, v -> showPersonalInfoBottomSheet());
        setupMenuItem(view.findViewById(R.id.menu_security), android.R.drawable.ic_lock_idle_lock, "Password & Security", "Password, 2FA & devices", R.color.tint_account_bg, R.color.tint_account_icon, v -> startActivity(new Intent(requireContext(), SecurityActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_login_activity), android.R.drawable.ic_menu_recent_history, "Login Activity", "", R.color.tint_account_bg, R.color.tint_account_icon, v -> startActivity(new Intent(requireContext(), LoginActivityHistoryActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_verification), android.R.drawable.checkbox_on_background, "Verification", "", R.color.tint_account_bg, R.color.tint_account_icon, v -> Toast.makeText(requireContext(), "Verification settings", Toast.LENGTH_SHORT).show());

        // SAFETY
        setupMenuItem(view.findViewById(R.id.menu_emergency_contacts), android.R.drawable.ic_menu_call, "Emergency Contacts", "Manage trusted emergency contacts", R.color.tint_safety_bg, R.color.tint_safety_icon, v -> Toast.makeText(requireContext(), "Go to Contacts tab", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_sos_settings), android.R.drawable.ic_menu_help, "SOS Settings", "Configure emergency/SOS preferences", R.color.tint_safety_bg, R.color.tint_safety_icon, v -> Toast.makeText(requireContext(), "SOS settings", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_location_sharing), android.R.drawable.ic_menu_mylocation, "Location Sharing", "Manage location sharing permissions", R.color.tint_safety_bg, R.color.tint_safety_icon, v -> Toast.makeText(requireContext(), "Location sharing", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_safety_notifications), android.R.drawable.ic_dialog_alert, "Safety Notifications", "Manage safety alerts and notifications", R.color.tint_safety_bg, R.color.tint_safety_icon, v -> Toast.makeText(requireContext(), "Safety notifications", Toast.LENGTH_SHORT).show());

        // APP & SETTINGS
        setupMenuItem(view.findViewById(R.id.menu_notifications), android.R.drawable.ic_popup_reminder, "Notifications", "Manage app notifications", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_appearance), android.R.drawable.ic_menu_view, "Appearance", "Light / Dark / System", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> Toast.makeText(requireContext(), "Appearance", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_language), android.R.drawable.ic_menu_sort_alphabetically, "Language", "Choose application language", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> Toast.makeText(requireContext(), "Language", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_app_preferences), android.R.drawable.ic_menu_preferences, "App Preferences", "Manage general application preferences", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> Toast.makeText(requireContext(), "App Preferences", Toast.LENGTH_SHORT).show());

        // PRIVACY
        setupMenuItem(view.findViewById(R.id.menu_privacy_settings), android.R.drawable.ic_secure, "Privacy Settings", "Control your profile and data visibility", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> startActivity(new Intent(requireContext(), PrivacySettingsActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_location_privacy), android.R.drawable.ic_menu_mylocation, "Location Privacy", "Manage location access and sharing", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> startActivity(new Intent(requireContext(), PrivacySettingsActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_contact_permissions), android.R.drawable.ic_menu_recent_history, "Contact Permissions", "Manage contact access", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> startActivity(new Intent(requireContext(), PrivacySettingsActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_privacy_policy), android.R.drawable.ic_menu_info_details, "Privacy Policy", "View privacy policy", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> showPrivacyPolicyDialog());

        // SUPPORT
        setupMenuItem(view.findViewById(R.id.menu_help_center), android.R.drawable.ic_menu_help, "Help Center", "Get help and find answers", R.color.tint_support_bg, R.color.tint_support_icon, v -> Toast.makeText(requireContext(), "Help Center", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_contact_support), android.R.drawable.ic_dialog_email, "Contact Support", "Send us a message", R.color.tint_support_bg, R.color.tint_support_icon, v -> Toast.makeText(requireContext(), "Contact Support", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_report_problem), android.R.drawable.ic_menu_report_image, "Report a Problem", "Report bugs or issues", R.color.tint_support_bg, R.color.tint_support_icon, v -> Toast.makeText(requireContext(), "Report a Problem", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_rate_app), android.R.drawable.star_on, "Rate the App", "Rate us on the Play Store", R.color.tint_support_bg, R.color.tint_support_icon, v -> Toast.makeText(requireContext(), "Rate the App", Toast.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menu_about_app), android.R.drawable.ic_dialog_info, "About the App", "", R.color.tint_support_bg, R.color.tint_support_icon, v -> Toast.makeText(requireContext(), "About Women Safety", Toast.LENGTH_SHORT).show());

        // BOTTOM ACTIONS
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
        view.findViewById(R.id.btn_delete_account).setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void setupMenuItem(View menuItem, int iconRes, String title, String subtitle, int bgTintRes, int iconTintRes, View.OnClickListener onClickListener) {
        if (menuItem == null) return;
        ImageView ivIcon = menuItem.findViewById(R.id.iv_menu_icon);
        View iconContainer = menuItem.findViewById(R.id.icon_container);
        TextView tvTitle = menuItem.findViewById(R.id.tv_menu_title);
        TextView tvSubtitle = menuItem.findViewById(R.id.tv_menu_subtitle);
        
        if (ivIcon != null) {
            ivIcon.setImageResource(iconRes);
            ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), iconTintRes)));
        }
        
        if (iconContainer != null) {
            iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bgTintRes)));
        }
        
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        }
        
        menuItem.setOnClickListener(onClickListener);
    }

    private void showPersonalInfoBottomSheet() {
        BottomSheetPersonalInfo bottomSheet = new BottomSheetPersonalInfo();
        bottomSheet.show(getParentFragmentManager(), "PersonalInfoBottomSheet");
    }

    private void showPrivacyPolicyDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("Your privacy is important to us.\n\n" +
                        "This app collects location data in the background to provide live tracking during emergencies. Your data is stored securely using Firebase and is only shared with your selected emergency contacts when an SOS is triggered.\n\n" +
                        "By using this app, you agree to our data collection and sharing practices as outlined.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void showLogoutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    authRepository.signOut();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete your account?")
                .setMessage("This action is permanent and cannot be undone. All your data, emergency contacts, and settings will be permanently erased.")
                .setPositiveButton("Delete Account", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Account deleted (dummy)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return email;
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        String prefix = phone.substring(0, 4);
        String suffix = phone.substring(phone.length() - 2);
        return prefix + "******" + suffix;
    }
}
