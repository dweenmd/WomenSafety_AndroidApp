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

        // Top App Bar
        View btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (requireActivity() instanceof com.dweenmd.womensafety.ui.MainActivity) {
                    ((com.dweenmd.womensafety.ui.MainActivity) requireActivity()).openDrawer();
                }
            });
        }
        ImageView ivTopAvatar = view.findViewById(R.id.iv_top_avatar);

        // Header Views
        TextView tvName = view.findViewById(R.id.tv_profile_header_name);
        TextView tvEmail = view.findViewById(R.id.tv_profile_header_email);
        TextView tvPhone = view.findViewById(R.id.tv_profile_header_phone);
        ImageView ivAvatar = view.findViewById(R.id.iv_profile_header_avatar);
        View btnEditAvatar = view.findViewById(R.id.btn_edit_avatar_header);
        View btnCompleteProfile = view.findViewById(R.id.btn_complete_profile);
        View cardProfileCompletion = view.findViewById(R.id.card_profile_completion);

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
                if (ivTopAvatar != null) {
                    Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivTopAvatar);
                }
            }
        });

        View.OnClickListener editProfileListener = v -> startActivity(new Intent(requireContext(), EditProfileActivity.class));
        btnEditAvatar.setOnClickListener(editProfileListener);
        btnCompleteProfile.setOnClickListener(editProfileListener);

        // ACCOUNT & SECURITY
        setupMenuItem(view.findViewById(R.id.menu_personal_info), android.R.drawable.ic_menu_info_details, "Personal Information", "View your profile details", R.color.tint_account_bg, R.color.tint_account_icon, v -> showPersonalInfoBottomSheet());
        setupMenuItem(view.findViewById(R.id.menu_security), android.R.drawable.ic_lock_idle_lock, "Password & Security", "Change password, view login history", R.color.tint_account_bg, R.color.tint_account_icon, v -> startActivity(new Intent(requireContext(), SecurityActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_verification), android.R.drawable.checkbox_on_background, "Verification", "Email and phone status", R.color.tint_account_bg, R.color.tint_account_icon, v -> startActivity(new Intent(requireContext(), VerificationActivity.class)));

        // APP PREFERENCES
        setupMenuItem(view.findViewById(R.id.menu_notifications), R.drawable.ic_notifications, "Notifications", "Manage app alerts and sounds", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> startActivity(new Intent(requireContext(), NotificationsSettingsActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_app_preferences), android.R.drawable.ic_menu_gallery, "Theme & Language", "Appearance and localization", R.color.tint_settings_bg, R.color.tint_settings_icon, v -> startActivity(new Intent(requireContext(), AppPreferencesActivity.class)));

        // SYSTEM & PERMISSIONS
        setupMenuItem(view.findViewById(R.id.menu_app_settings), android.R.drawable.ic_menu_preferences, "General Settings", "App-wide system settings", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
        setupMenuItem(view.findViewById(R.id.menu_permissions), android.R.drawable.ic_menu_mylocation, "Permissions", "Manage required system access", R.color.tint_privacy_bg, R.color.tint_privacy_icon, v -> startActivity(new Intent(requireContext(), PermissionsActivity.class)));

        // ACTIONS
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
                    authRepository.deleteAccount(new AuthRepository.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(requireContext(), LoginActivity.class));
                                requireActivity().finish();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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

