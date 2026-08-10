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
        // Read contacts immediately from local cache so we don't block on network
        List<ContactsRepository.Contact> contacts = contactsRepository.getLocalContactsSync();
        if (contacts == null || contacts.isEmpty()) {
            callback.onFailure("No emergency contacts found.");
            return;
        }

        // Fetch location
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
    }

    private void sendMessages(List<ContactsRepository.Contact> contacts, String locationText, SosCallback callback) {
        String message = "Emergency! I'm in trouble!\nPlease help me ASAP.\nMy current location: " + locationText;

        Intent sentIntent = new Intent(ACTION_SMS_SENT);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent deliveredIntent = new Intent(ACTION_SMS_DELIVERED);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE);

        for (ContactsRepository.Contact contact : contacts) {
            try {
                smsManager.sendTextMessage(contact.phone, null, message, sentPI, deliveredPI);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + contact.name, e);
            }
        }
        callback.onSosTriggered("SOS triggered. Waiting for delivery confirmation.");
    }

    public interface SosCallback {
        void onSosTriggered(String status);
        void onFailure(String error);
    }
}
