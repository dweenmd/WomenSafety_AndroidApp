package com.dweenmd.womensafety.sos;

import com.dweenmd.womensafety.data.ContactsRepository;

/**
 * Interface to send push alerts via a backend service.
 * This ensures contacts with the app installed receive a reliable push notification 
 * alongside the standard SMS fallback.
 */
public interface AlertBackendClient {

    /**
     * Sends a push notification alert to the specified contact's device.
     * 
     * @param contact The emergency contact to notify.
     * @param message The alert message.
     */
    void sendPushAlert(ContactsRepository.Contact contact, String message);
    
    // TODO: Implement this interface with Retrofit or similar HTTP client 
    // to POST to your actual backend endpoint that interfaces with Firebase Cloud Messaging.
    // e.g., POST https://your-backend.com/api/send-alert
}
