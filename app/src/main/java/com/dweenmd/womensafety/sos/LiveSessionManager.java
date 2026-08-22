package com.dweenmd.womensafety.sos;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.service.LiveLocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Starts/stops live-location sessions and distributes the tracking link via
 * SMS (automatic) plus WhatsApp / other apps such as imo (one-tap intents —
 * Android does not allow apps to send WhatsApp/imo messages fully automatically).
 */
public class LiveSessionManager {

    private static final String TAG = "LiveSessionManager";
    // The viewer page is hosted on Firebase Hosting; deploy with `firebase deploy`.
    private static final String VIEWER_URL = "https://women-safety-6af54.web.app/live.html?session=";

    public interface LiveSessionCallback {
        void onStarted(String sessionId, String shareUrl, int smsSentCount);
        void onFailure(String error);
    }

    private final Context context;
    private final ContactsRepository contactsRepository;

    public LiveSessionManager(Context context) {
        this.context = context;
        this.contactsRepository = new ContactsRepository(context);
    }

    public static boolean isSharing(Context context) {
        return LiveLocationService.isSharing(context);
    }

    public String buildShareUrl(String sessionId) {
        return VIEWER_URL + sessionId;
    }

    public void startLiveSession(LiveSessionCallback callback) {
        if (isSharing(context)) {
            callback.onFailure("Live sharing is already running.");
            return;
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String shareUrl = buildShareUrl(sessionId);

        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        if (contacts == null || contacts.isEmpty()) {
            callback.onFailure("No emergency contacts saved. Add contacts first.");
            return;
        }

        String message = "I'm sharing my live location with you (updates in real time):\n" + shareUrl
                + "\n— sent from Women Safety app";

        int smsSent = 0;
        if (hasSmsPermission()) {
            SmsManager smsManager = SmsSimManager.resolveSmsManager(context);
            for (ContactsRepository.Contact contact : contacts) {
                try {
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    if (parts.size() > 1) {
                        smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null);
                    } else {
                        smsManager.sendTextMessage(contact.phone, null, message, null, null);
                    }
                    smsSent++;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to SMS live link to " + contact.name, e);
                }
            }
        }

        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String ownerName = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName() : "A Women Safety user";

        Intent serviceIntent = new Intent(context, LiveLocationService.class);
        serviceIntent.putExtra(LiveLocationService.EXTRA_SESSION_ID, sessionId);
        serviceIntent.putExtra(LiveLocationService.EXTRA_OWNER_NAME, ownerName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        SosAlertNotifier.notify(context);
        callback.onStarted(sessionId, shareUrl, smsSent);
    }

    public void stopLiveSession() {
        Intent stopIntent = new Intent(context, LiveLocationService.class);
        stopIntent.setAction(LiveLocationService.ACTION_STOP);
        context.startService(stopIntent);
    }

    /**
     * Opens a WhatsApp chat with the number, message prefilled — the user just
     * taps send. WhatsApp offers no fully-automatic send API.
     */
    public void shareViaWhatsApp(String phone, String shareUrl) {
        String e164 = phone.replaceAll("[^0-9]", "");
        String message = "I'm sharing my live location with you (updates in real time):\n" + shareUrl;
        try {
            Uri uri = Uri.parse("https://wa.me/" + e164 + "?text=" + Uri.encode(message));
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "WhatsApp not installed", e);
        }
    }

    /** Generic share sheet — user can pick imo, Messenger, email, etc. */
    public void shareViaOtherApps(String shareUrl) {
        String message = "I'm sharing my live location with you (updates in real time):\n" + shareUrl;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(send, "Share live location")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private boolean hasSmsPermission() {
        return androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}
