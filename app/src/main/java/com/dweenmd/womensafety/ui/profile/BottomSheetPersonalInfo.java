package com.dweenmd.womensafety.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseUser;

public class BottomSheetPersonalInfo extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.tv_bs_name);
        TextView tvEmail = view.findViewById(R.id.tv_bs_email);
        TextView tvPhone = view.findViewById(R.id.tv_bs_phone);

        AuthRepository authRepository = new AuthRepository(requireContext());
        FirebaseUser user = authRepository.getCurrentUser().getValue();

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");
            tvEmail.setText(maskEmail(user.getEmail()));
            
            String phone = user.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                tvPhone.setText(maskPhone(phone));
            } else {
                tvPhone.setText("Phone not set");
            }
        }

        view.findViewById(R.id.btn_bs_edit_info).setOnClickListener(v -> {
            dismiss();
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        });
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
