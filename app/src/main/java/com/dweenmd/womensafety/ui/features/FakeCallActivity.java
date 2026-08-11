package com.dweenmd.womensafety.ui.features;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dweenmd.womensafety.R;

public class FakeCallActivity extends AppCompatActivity {

    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show over lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                
        setContentView(R.layout.activity_fake_call);

        TextView tvStatus = findViewById(R.id.tv_caller_status);
        ImageButton btnAnswer = findViewById(R.id.btn_answer);
        ImageButton btnDecline = findViewById(R.id.btn_decline);

        playRingtone();

        btnAnswer.setOnClickListener(v -> {
            stopRingtone();
            tvStatus.setText(R.string.fake_call_timer_zero);
            btnAnswer.setVisibility(android.view.View.GONE);
            // Simulate conversation delay then hang up
            new Handler().postDelayed(this::finish, 5000);
        });

        btnDecline.setOnClickListener(v -> {
            stopRingtone();
            finish();
        });
    }

    private void playRingtone() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        stopRingtone();
        super.onDestroy();
    }
}
