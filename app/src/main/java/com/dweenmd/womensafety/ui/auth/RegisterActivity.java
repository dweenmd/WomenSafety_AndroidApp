package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.ui.MainActivity;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerUser());
        
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            return;
        }
        if (!isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters and include uppercase, lowercase, number, and special character");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        authRepository.signUpWithDetails(name, email, phone, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, R.string.toast_registration_successful, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 4096) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasNumber && hasSpecial;
    }
}
