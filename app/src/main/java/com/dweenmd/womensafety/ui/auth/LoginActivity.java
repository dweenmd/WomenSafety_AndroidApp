package com.dweenmd.womensafety.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);
        Button btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        Button btnPhoneSignIn = findViewById(R.id.btn_phone_signin);

        View mainContent = findViewById(R.id.cl_main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0f);
            mainContent.setTranslationY(50f);
            mainContent.animate().alpha(1f).translationY(0f).setDuration(400).start();
        }

        btnLogin.setOnClickListener(v -> loginUser());
        
        View tvCreateAccount = findViewById(R.id.tv_create_account);
        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        btnPhoneSignIn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, PhoneLoginActivity.class));
        });

        setupButtonAnimation(btnLogin);
        setupButtonAnimation(btnGoogleSignIn);
        setupButtonAnimation(btnPhoneSignIn);
    }

    private void setupButtonAnimation(View button) {
        if (button == null) return;
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
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
                Toast.makeText(this, "Google Sign-In Failed! Add your SHA-1 key to Firebase.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        android.widget.TextView tvLoginBtnText = findViewById(R.id.tv_login_btn_text);
        if (tvLoginBtnText != null) tvLoginBtnText.setVisibility(View.GONE);

        AuthRepository.AuthCallback callback = new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                if (tvLoginBtnText != null) tvLoginBtnText.setVisibility(View.VISIBLE);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                if (tvLoginBtnText != null) tvLoginBtnText.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, "Auth Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        authRepository.signInWithEmailAndPassword(email, password, callback);
    }
}
