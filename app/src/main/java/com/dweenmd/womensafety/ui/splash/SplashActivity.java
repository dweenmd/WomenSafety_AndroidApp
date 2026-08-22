package com.dweenmd.womensafety.ui.splash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.dweenmd.womensafety.ui.MainActivity;
import com.dweenmd.womensafety.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        AuthRepository authRepository = new AuthRepository(this);

        timer = new CountDownTimer(1000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    routeToNextScreen(authRepository);
                }
            }
        }.start();
    }

    private void routeToNextScreen(AuthRepository authRepository) {
        Class<?> next;

        if (authRepository.isDemoUser()) {
            next = MainActivity.class;
        } else if (!com.dweenmd.womensafety.ui.auth.PrivacyConsentActivity.hasConsent(this)) {
            // Consent must be collected before any session, including
            // pre-existing logins, which previously bypassed it.
            next = com.dweenmd.womensafety.ui.auth.PrivacyConsentActivity.class;
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            next = (user != null) ? MainActivity.class : LoginActivity.class;
        }

        startActivity(new Intent(this, next));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }
}
