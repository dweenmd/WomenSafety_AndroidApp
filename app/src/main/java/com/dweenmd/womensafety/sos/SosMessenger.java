package com.dweenmd.womensafety.sos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;

import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.data.LocationRepository;

import java.util.ArrayList;
import java.util.List;

public class SosMessenger {

    private static final String TAG = "SosMessenger";

    private final Context context;
    private final ContactsRepository contactsRepository;
    private final LocationRepository locationRepository;

    public SosMessenger(Context context) {
        this.context = context;
        this.contactsRepository = new ContactsRepository(context);
        this.locationRepository = new LocationRepository(context);
    }

    public void triggerSos(SosCallback callback) {
        if (!hasPermission(android.Manifest.permission.SEND_SMS)) {
            callback.onFailure("SMS permission not granted. Action aborted.");
            return;
        }

        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        if (contacts == null || contacts.isEmpty()) {
            callback.onFailure("No emergency contacts saved. Add contacts first.");
            return;
        }

        SosAlertNotifier.notify(context);

        android.content.SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean includeLocation = prefs.getBoolean("liveLocationOnSos", true);

        if (includeLocation) {
            locationRepository.getCurrentLocation(new LocationRepository.LocationCallbackResult() {
                @Override
                public void onSuccess(Location location) {
                    String locUrl = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                    sendMessages(contacts, locUrl, callback);
                }

                @Override
                public void onFailure(String reason) {
                    Log.w(TAG, "Location fetch failed: " + reason);
                    sendMessages(contacts, "Location unavailable: " + reason, callback);
                }
            });
        } else {
            sendMessages(contacts, "Location sharing is disabled by user.", callback);
        }
    }

    /**
     * Opens the dialer with the local emergency number pre-filled; the user taps
     * the call button. Deliberately uses ACTION_DIAL (no permission, no auto-dial)
     * — auto-calling emergency services on a false shake trigger is dangerous and
     * silently fails on Android anyway (CALL_PRIVILEGED is never granted).
     */
    public void dialEmergencyNumber() {
        try {
            EmergencyNumberProvider.EmergencyNumbers numbers = EmergencyNumberProvider.getEmergencyNumber(context);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(android.net.Uri.parse("tel:" + numbers.general));
            dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open emergency dialer", e);
        }
    }

    public void shareLocationOnly(SosCallback callback) {
        if (!hasPermission(android.Manifest.permission.SEND_SMS)) {
            callback.onFailure("SMS permission not granted. Action aborted.");
            return;
        }

        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        if (contacts == null || contacts.isEmpty()) {
            callback.onFailure("No emergency contacts found.");
            return;
        }

        locationRepository.getCurrentLocation(new LocationRepository.LocationCallbackResult() {
            @Override
            public void onSuccess(Location location) {
                String locUrl = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                String message = "Here is my current Live Location link:\n" + locUrl;
                sendMessages(contacts, message, callback);
            }

            @Override
            public void onFailure(String reason) {
                callback.onFailure("Failed to get location: " + reason);
            }
        });
    }

    private void sendMessages(List<ContactsRepository.Contact> contacts, String locationText, SosCallback callback) {
        String message = "Emergency! I'm in trouble!\nPlease help me ASAP.\nMy current location: " + locationText;

        // TODO: push alerts via a real backend (AlertBackendClient/FCM) are not implemented yet.
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        int sentCount = 0;

        for (ContactsRepository.Contact contact : contacts) {
            try {
                // Location messages exceed one SMS segment; use multipart so long
                // messages don't fail with a generic error.
                ArrayList<String> parts = smsManager.divideMessage(message);
                if (parts.size() > 1) {
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null);
                } else {
                    smsManager.sendTextMessage(contact.phone, null, message, null, null);
                }
                sentCount++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + contact.name + " (" + contact.phone + ")", e);
            }
        }

        if (sentCount == 0) {
            callback.onFailure("SOS failed: could not send SMS to any contact.");
        } else if (sentCount < contacts.size()) {
            callback.onSosTriggered("SOS sent to " + sentCount + " of " + contacts.size() + " contacts.");
        } else {
            callback.onSosTriggered("SOS sent to all " + sentCount + " contacts.");
        }
    }

    private boolean hasPermission(String permission) {
        return androidx.core.content.ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public interface SosCallback {
        void onSosTriggered(String status);
        void onFailure(String error);
    }
}
