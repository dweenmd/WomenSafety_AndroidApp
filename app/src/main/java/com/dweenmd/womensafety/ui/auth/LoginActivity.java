package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private AuthRepository authRepository;
    private ProgressBar progressBar;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister, btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        progressBar = findViewById(R.id.progress_bar);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);

        btnLogin.setOnClickListener(v -> performAuth(true));
        btnRegister.setOnClickListener(v -> performAuth(false));
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = authRepository.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authRepository.firebaseAuthWithGoogle(account.getIdToken(), new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Google Auth Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                Log.w("LoginActivity", "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In Failed: You need a valid google-services.json", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void performAuth(boolean isLogin) {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        AuthRepository.AuthCallback callback = new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Auth Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        if (isLogin) {
            authRepository.signInWithEmailAndPassword(email, password, callback);
        } else {
            authRepository.signUpWithEmailAndPassword(email, password, callback);
        }
    }
}
