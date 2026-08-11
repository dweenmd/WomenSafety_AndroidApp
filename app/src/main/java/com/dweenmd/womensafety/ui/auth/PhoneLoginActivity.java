package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.ui.MainActivity;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.telephony.TelephonyManager;
import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnSendOtp;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private Spinner spinnerCountry;
    private PhoneNumberUtil phoneUtil;
    private List<String> regionCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        authRepository = new AuthRepository(this);
        phoneUtil = PhoneNumberUtil.getInstance();

        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        progressBar = findViewById(R.id.progressBar);
        spinnerCountry = findViewById(R.id.spinnerCountry);

        setupCountrySpinner();

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void setupCountrySpinner() {
        Set<String> supportedRegions = phoneUtil.getSupportedRegions();
        regionCodes = new ArrayList<>(supportedRegions);
        Collections.sort(regionCodes);

        List<String> displayNames = new ArrayList<>();
        for (String region : regionCodes) {
            int callingCode = phoneUtil.getCountryCodeForRegion(region);
            displayNames.add(getCountryFlag(region) + " " + region + " (+" + callingCode + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayNames);
        spinnerCountry.setAdapter(adapter);

        // Set default selection
        String defaultRegion = getDefaultRegion();
        if (defaultRegion != null && regionCodes.contains(defaultRegion)) {
            spinnerCountry.setSelection(regionCodes.indexOf(defaultRegion));
        }
    }

    private String getDefaultRegion() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null && tm.getSimCountryIso() != null && !tm.getSimCountryIso().isEmpty()) {
            return tm.getSimCountryIso().toUpperCase(Locale.US);
        }
        return Locale.getDefault().getCountry().toUpperCase(Locale.US);
    }

    private String getCountryFlag(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) return "";
        int flagOffset = 0x1F1E6;
        int asciiOffset = 0x41;
        int firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset;
        int secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    private void sendOtp() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return;
        }

        int selectedPosition = spinnerCountry.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= regionCodes.size()) return;
        String selectedRegion = regionCodes.get(selectedPosition);

        String formattedPhone;
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phone, selectedRegion);
            if (!phoneUtil.isValidNumber(number)) {
                etPhone.setError("Invalid phone number for this region");
                return;
            }
            formattedPhone = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            etPhone.setError("Invalid phone number format");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSendOtp.setEnabled(false);

        authRepository.verifyPhoneNumber(formattedPhone, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Auto-retrieval or Instant verification succeeded
                authRepository.signInWithPhoneAuthCredential(credential, new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
                        finishAffinity();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        btnSendOtp.setEnabled(true);
                        Toast.makeText(PhoneLoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                btnSendOtp.setEnabled(true);
                Toast.makeText(PhoneLoginActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressBar.setVisibility(View.GONE);
                btnSendOtp.setEnabled(true);
                
                Intent intent = new Intent(PhoneLoginActivity.this, OtpVerificationActivity.class);
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
            }
        });
    }
}
