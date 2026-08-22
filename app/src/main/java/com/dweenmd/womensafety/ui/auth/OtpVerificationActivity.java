package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.ui.MainActivity;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {

    private String verificationId;
    private EditText etOtp;
    private Button btnVerify;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        verificationId = getIntent().getStringExtra("verificationId");

        authRepository = new AuthRepository(this);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        if (verificationId == null) {
            Toast.makeText(this, "Verification session expired. Please request a new code.", Toast.LENGTH_LONG).show();
            return;
        }

        String code = etOtp.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            etOtp.setError("Enter 6-digit OTP");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        
        authRepository.signInWithPhoneAuthCredential(credential, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OtpVerificationActivity.this, R.string.toast_login_successful, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                Toast.makeText(OtpVerificationActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
