package com.dweenmd.womensafety.sos;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.dweenmd.womensafety.R;

/**
 * Shared long-press (1.5s) trigger logic for the SOS buttons on the Home and
 * Safety screens; previously duplicated verbatim in both fragments.
 */
public class SosButtonController {

    private static final long TRIGGER_HOLD_MS = 1500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable triggerRunnable;
    private boolean isHolding = false;

    public SosButtonController(View button, View pulseBg, Runnable onTrigger, View.OnClickListener onTap) {
        if (pulseBg != null) {
            Animation pulse = AnimationUtils.loadAnimation(pulseBg.getContext(), R.anim.pulse);
            pulseBg.startAnimation(pulse);
        }

        triggerRunnable = () -> {
            if (isHolding) {
                isHolding = false;
                onTrigger.run();
            }
        };

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    button.animate().scaleX(0.92f).scaleY(0.92f).setDuration(150).start();
                    handler.postDelayed(triggerRunnable, TRIGGER_HOLD_MS);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    handler.removeCallbacks(triggerRunnable);
                    if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                    return true;
            }
            return false;
        });

        button.setOnClickListener(onTap);
    }

    /** Call from onDestroyView to drop pending callbacks and avoid leaking the view. */
    public void destroy() {
        isHolding = false;
        handler.removeCallbacksAndMessages(null);
    }
}
