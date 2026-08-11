package com.dweenmd.womensafety.sos;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;

import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.data.LocationRepository;

import java.util.List;

public class SosMessenger {

    private static final String TAG = "SosMessenger";
    public static final String ACTION_SMS_SENT = "com.dweenmd.womensafety.SMS_SENT";
    public static final String ACTION_SMS_DELIVERED = "com.dweenmd.womensafety.SMS_DELIVERED";

    private final Context context;
    private final ContactsRepository contactsRepository;
    private final LocationRepository locationRepository;
    private final SmsManager smsManager;

    public SosMessenger(Context context) {
        this.context = context;
        this.contactsRepository = new ContactsRepository(context);
        this.locationRepository = new LocationRepository(context);
        this.smsManager = SmsManager.getDefault();
    }

    public void triggerSos(SosCallback callback) {
        // Double check permissions before proceeding
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            callback.onFailure("SMS Permission not granted. Action aborted.");
            return;
        }

        // Call the emergency number synchronously with the user action to avoid Android 10+ background start restrictions
        callEmergencyNumber();

        // Read contacts immediately from local cache so we don't block on network
        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        
        android.content.SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean includeLocation = prefs.getBoolean("liveLocationOnSos", true);

        if (includeLocation) {
            // Fetch location
            locationRepository.getCurrentLocation(new LocationRepository.LocationCallbackResult() {
                @Override
                public void onSuccess(Location location) {
                    // Start background recording
                    new AudioRecorderHelper(context).startRecording();
                    
                    String locUrl = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                    
                    if (contacts != null && !contacts.isEmpty()) {
                        sendMessages(contacts, locUrl, callback);
                    } else {
                        callback.onFailure("No contacts found. Called emergency number.");
                    }
                }

                @Override
                public void onFailure(String reason) {
                    // Start background recording even if location fails
                    new AudioRecorderHelper(context).startRecording();
                    
                    Log.w(TAG, "Location fetch failed: " + reason);
                    
                    if (contacts != null && !contacts.isEmpty()) {
                        sendMessages(contacts, "Location unavailable: " + reason, callback);
                    } else {
                        callback.onFailure("No contacts found. Called emergency number.");
                    }
                }
            });
        } else {
            // Send SOS without location
            new AudioRecorderHelper(context).startRecording();
            if (contacts != null && !contacts.isEmpty()) {
                sendMessages(contacts, "Location sharing is disabled by user.", callback);
            } else {
                callback.onFailure("No contacts found. Called emergency number.");
            }
        }
    }

    private void callEmergencyNumber() {
        try {
            EmergencyNumberProvider.EmergencyNumbers numbers = EmergencyNumberProvider.getEmergencyNumber(context);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + numbers.general));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start ACTION_CALL", e);
        }
    }

    public void shareLocationOnly(SosCallback callback) {
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
                for (ContactsRepository.Contact contact : contacts) {
                    try {
                        smsManager.sendTextMessage(contact.phone, null, message, null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to send Location SMS to " + contact.name, e);
                    }
                }
                callback.onSosTriggered("Live Location link sent to contacts.");
            }

            @Override
            public void onFailure(String reason) {
                callback.onFailure("Failed to get location: " + reason);
            }
        });
    }

    private void sendMessages(List<ContactsRepository.Contact> contacts, String locationText, SosCallback callback) {
        String message = "Emergency! I'm in trouble!\nPlease help me ASAP.\nMy current location: " + locationText;

        Intent sentIntent = new Intent(ACTION_SMS_SENT);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent deliveredIntent = new Intent(ACTION_SMS_DELIVERED);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE);
        
        // TODO: Replace this stub with a real backend client (e.g. Retrofit instance) when backend is ready
        // TODO: Ensure google-services.json is added to the app/ directory for FCM to work.
        AlertBackendClient alertClient = new AlertBackendClient() {
            @Override
            public void sendPushAlert(ContactsRepository.Contact contact, String msg) {
                Log.d(TAG, "Stub: Sending push alert to contact " + contact.name + " (" + contact.phone + ")");
                // Actual implementation would POST to your server which then sends an FCM message
            }
        };

        for (ContactsRepository.Contact contact : contacts) {
            // 1. Primary channel: SMS
            try {
                smsManager.sendTextMessage(contact.phone, null, message, sentPI, deliveredPI);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + contact.name, e);
            }
            
            // 2. Parallel channel: Push Notification (FCM)
            try {
                alertClient.sendPushAlert(contact, message);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send push alert to " + contact.name, e);
            }
        }
        callback.onSosTriggered("SOS triggered. SMS & Push alerts sent.");
    }

    public interface SosCallback {
        void onSosTriggered(String status);
        void onFailure(String error);
    }
}
