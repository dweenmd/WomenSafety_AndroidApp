package com.dweenmd.womensafety.sos;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Dual-SIM support: lets the user pick which SIM sends the SOS SMS.
 * The preferred subscription id lives in "app_settings" (sms_sim_sub_id);
 * -1 means "use the system default SIM".
 */
public final class SmsSimManager {

    private static final String TAG = "SmsSimManager";
    private static final String PREFS = "app_settings";
    private static final String KEY_SUB_ID = "sms_sim_sub_id";

    private SmsSimManager() {}

    public static class SimInfo {
        public final int subId;
        public final int slotIndex;
        public final String carrierName;
        public final String displayName;

        public SimInfo(int subId, int slotIndex, String carrierName, String displayName) {
            this.subId = subId;
            this.slotIndex = slotIndex;
            this.carrierName = carrierName;
            this.displayName = displayName;
        }

        /** "SIM 1 — Grameenphone" */
        public String label() {
            String carrier = (carrierName != null && !carrierName.isEmpty()) ? carrierName
                    : (displayName != null ? displayName : "");
            return "SIM " + (slotIndex + 1) + (carrier.isEmpty() ? "" : " — " + carrier);
        }
    }

    public static boolean hasPhoneStatePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Returns the active SIMs; empty list if permission missing or single-SIM device. */
    public static List<SimInfo> getActiveSims(Context context) {
        List<SimInfo> sims = new ArrayList<>();
        if (!hasPhoneStatePermission(context)) return sims;
        try {
            SubscriptionManager sm = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (sm == null) return sims;
            List<SubscriptionInfo> subs = sm.getActiveSubscriptionInfoList();
            if (subs != null) {
                for (SubscriptionInfo si : subs) {
                    sims.add(new SimInfo(si.getSubscriptionId(), si.getSimSlotIndex(),
                            si.getCarrierName() != null ? si.getCarrierName().toString() : "",
                            si.getDisplayName() != null ? si.getDisplayName().toString() : ""));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not read subscriptions", e);
        }
        return sims;
    }

    public static long getPreferredSubId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_SUB_ID, -1L);
    }

    public static void setPreferredSubId(Context context, long subId) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putLong(KEY_SUB_ID, subId).apply();
    }

    public static String getPreferredSimLabel(Context context) {
        long subId = getPreferredSubId(context);
        if (subId == -1L) return "Default SIM (system)";
        for (SimInfo sim : getActiveSims(context)) {
            if (sim.subId == subId) return sim.label();
        }
        return "Default SIM (system)";
    }

    /**
     * SmsManager bound to the preferred SIM. Uses the public API on 31+ and a
     * reflection fallback on 26-30 (the per-subscription getDefault is hidden there);
     * any failure falls back to the default SmsManager.
     */
    public static SmsManager resolveSmsManager(Context context) {
        long subId = getPreferredSubId(context);
        if (subId == -1L) {
            return context.getSystemService(SmsManager.class);
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                return SmsManager.getSmsManagerForSubscriptionId((int) subId);
            }
            // Reflection for API 26-30
            try {
                Method m = SmsManager.class.getMethod("getDefault", long.class);
                Object result = m.invoke(null, subId);
                if (result instanceof SmsManager) return (SmsManager) result;
            } catch (Exception ignored) {
            }
            Method m = SmsManager.class.getMethod("getDefault", int.class);
            Object result = m.invoke(null, (int) subId);
            if (result instanceof SmsManager) return (SmsManager) result;
        } catch (Exception e) {
            Log.w(TAG, "Per-SIM SmsManager failed; using default SIM", e);
        }
        return context.getSystemService(SmsManager.class);
    }
}
