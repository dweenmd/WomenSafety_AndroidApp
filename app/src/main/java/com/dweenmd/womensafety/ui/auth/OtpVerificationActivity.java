package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.ui.MainActivity;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.data.FirebaseAuthErrors;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final long RESEND_COOLDOWN_MS = 60_000;

    private String verificationId;
    private EditText etOtp;
    private Button btnVerify;
    private ProgressBar progressBar;
    private TextView tvResend;
    private CountDownTimer resendTimer;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Intent extra first (backward compat), then the session holder with the resend token.
        verificationId = getIntent().getStringExtra("verificationId");
        if (verificationId == null) {
            verificationId = OtpSessionHolder.verificationId;
        }

        authRepository = new AuthRepository(this);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);
        tvResend = findViewById(R.id.tv_resend_otp);

        btnVerify.setOnClickListener(v -> verifyOtp());
        tvResend.setOnClickListener(v -> resendCode());
        startResendCountdown();
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        super.onDestroy();
    }

    private void verifyOtp() {
        if (verificationId == null) {
            Toast.makeText(this, "Verification session expired. Please go back and request a new code.", Toast.LENGTH_LONG).show();
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
                OtpSessionHolder.clear();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OtpVerificationActivity.this, R.string.toast_login_successful, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                Toast.makeText(OtpVerificationActivity.this,
                        "Verification failed: " + FirebaseAuthErrors.friendly(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        String phone = OtpSessionHolder.phoneNumber;
        if (phone == null) {
            Toast.makeText(this, "Cannot resend — please go back and re-enter your number.", Toast.LENGTH_LONG).show();
            return;
        }

        tvResend.setEnabled(false);
        authRepository.verifyPhoneNumber(phone, this, OtpSessionHolder.resendToken,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Auto-retrieval: sign in directly.
                        authRepository.signInWithPhoneAuthCredential(credential, new AuthRepository.AuthCallback() {
                            @Override
                            public void onSuccess(FirebaseUser user) {
                                OtpSessionHolder.clear();
                                startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
                                finishAffinity();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OtpVerificationActivity.this,
                                        FirebaseAuthErrors.friendly(e), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        tvResend.setEnabled(true);
                        Toast.makeText(OtpVerificationActivity.this,
                                FirebaseAuthErrors.friendly(e), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String newVerificationId, PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        OtpSessionHolder.verificationId = newVerificationId;
                        OtpSessionHolder.resendToken = token;
                        etOtp.setText("");
                        Toast.makeText(OtpVerificationActivity.this, "New code sent", Toast.LENGTH_SHORT).show();
                        startResendCountdown();
                    }
                });
    }

    private void startResendCountdown() {
        tvResend.setEnabled(false);
        if (resendTimer != null) resendTimer.cancel();
        resendTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResend.setText(getString(R.string.otp_resend_in, (int) (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                tvResend.setEnabled(true);
                tvResend.setText(R.string.otp_resend_available);
            }
        }.start();
    }
}
