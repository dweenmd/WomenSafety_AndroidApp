package com.dweenmd.womensafety.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsRepository {

    private static final String TAG = "ContactsRepository";
    private static final String PREF_NAME = "secure_contacts_prefs";
    
    private SharedPreferences sharedPreferences;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final MutableLiveData<List<Contact>> contactsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public ContactsRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
        }
        
        loadLocalContacts();
        syncWithFirestore();
    }

    public LiveData<List<Contact>> getContacts() {
        return contactsLiveData;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public void saveContact(int index, String name, String phone) {
        if (!PhoneNumberValidator.isValid(phone)) {
            Log.e(TAG, "Invalid phone number: " + phone);
            return;
        }

        // Save locally first for instant UI response and offline fallback
        String contactId = "contact" + index;
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putString(contactId + "_name", name)
                    .putString(contactId + "_phone", phone)
                    .apply();
        }
        
        loadLocalContacts(); // Update LiveData immediately

        // Sync to Firestore
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            isSyncing.setValue(true);
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("name", name);
            contactData.put("phone", phone);
            contactData.put("addedAt", System.currentTimeMillis());

            db.collection("users").document(user.getUid())
                    .collection("contacts").document(contactId)
                    .set(contactData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Contact saved to Firestore successfully");
                        isSyncing.setValue(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving contact to Firestore", e);
                        isSyncing.setValue(false);
                        // Data is still safe in EncryptedSharedPreferences and will be synced later
                        // if Firestore persistence is enabled.
                    });
        }
    }

    public List<Contact> getLocalContactsSync() {
        List<Contact> contacts = new ArrayList<>();
        if (sharedPreferences != null) {
            // App supports up to 2 contacts as per current logic
            for (int i = 1; i <= 2; i++) {
                String contactId = "contact" + i;
                String name = sharedPreferences.getString(contactId + "_name", null);
                String phone = sharedPreferences.getString(contactId + "_phone", null);
                if (phone != null && !phone.isEmpty()) {
                    contacts.add(new Contact(contactId, name != null ? name : "Emergency Contact " + i, phone));
                }
            }
        }
        return contacts;
    }

    private void loadLocalContacts() {
        contactsLiveData.setValue(getLocalContactsSync());
    }

    public void syncWithFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        isSyncing.setValue(true);
        db.collection("users").document(user.getUid()).collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (sharedPreferences != null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        queryDocumentSnapshots.forEach(doc -> {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String phone = doc.getString("phone");
                            
                            editor.putString(id + "_name", name);
                            editor.putString(id + "_phone", phone);
                        });
                        editor.apply();
                        loadLocalContacts();
                    }
                    isSyncing.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync contacts from Firestore", e);
                    isSyncing.setValue(false);
                });
    }

    public static class Contact {
        public String id;
        public String name;
        public String phone;

        public Contact(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }
    }
}
