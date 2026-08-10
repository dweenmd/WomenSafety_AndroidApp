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
import java.util.UUID;

public class ContactsRepository {

    private static final String TAG = "ContactsRepository";
    private static final String PREF_NAME = "secure_contacts_prefs";
    
    private SharedPreferences sharedPreferences;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final AuthRepository authRepo;

    private final MutableLiveData<List<Contact>> contactsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public ContactsRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authRepo = new AuthRepository(context);
        
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

    public void saveContact(Contact contact) {
        if (!PhoneNumberValidator.isValid(contact.phone)) {
            Log.e(TAG, "Invalid phone number skipped: " + contact.phone);
            return;
        }

        if (contact.id == null || contact.id.isEmpty()) {
            contact.id = UUID.randomUUID().toString();
        }

        if (contact.isPrimary) {
            // Remove primary from others
            List<Contact> allContacts = getLocalContactsSync();
            for (Contact c : allContacts) {
                if (!c.id.equals(contact.id) && c.isPrimary) {
                    c.isPrimary = false;
                    saveContactInternal(c);
                }
            }
        }

        saveContactInternal(contact);
        loadLocalContacts();

        // Sync to Firestore
        if (authRepo.isDemoUser()) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("name", contact.name);
            contactData.put("phone", contact.phone);
            contactData.put("relationship", contact.relationship);
            contactData.put("isPrimary", contact.isPrimary);
            contactData.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(user.getUid())
                    .collection("contacts").document(contact.id)
                    .set(contactData, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving contact to Firestore", e));
        }
    }

    private void saveContactInternal(Contact c) {
        if (sharedPreferences == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        String currentIds = sharedPreferences.getString("contact_ids", "");
        if (!currentIds.contains(c.id)) {
            if (currentIds.length() > 0) currentIds += ",";
            currentIds += c.id;
            editor.putString("contact_ids", currentIds);
        }

        editor.putString(c.id + "_name", c.name);
        editor.putString(c.id + "_phone", c.phone);
        editor.putString(c.id + "_relationship", c.relationship);
        editor.putBoolean(c.id + "_isPrimary", c.isPrimary);
        editor.apply();
    }

    public void deleteContact(String contactId) {
        if (sharedPreferences == null) return;
        
        String currentIds = sharedPreferences.getString("contact_ids", "");
        String[] idArray = currentIds.split(",");
        StringBuilder newIds = new StringBuilder();
        
        for (String id : idArray) {
            if (!id.isEmpty() && !id.equals(contactId)) {
                if (newIds.length() > 0) newIds.append(",");
                newIds.append(id);
            }
        }
        
        sharedPreferences.edit()
                .remove(contactId + "_name")
                .remove(contactId + "_phone")
                .remove(contactId + "_relationship")
                .remove(contactId + "_isPrimary")
                .putString("contact_ids", newIds.toString())
                .apply();
                
        loadLocalContacts();
        
        if (!authRepo.isDemoUser()) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                db.collection("users").document(user.getUid())
                        .collection("contacts").document(contactId)
                        .delete();
            }
        }
    }

    public List<Contact> getLocalContactsSync() {
        List<Contact> contacts = new ArrayList<>();
        if (sharedPreferences != null) {
            String currentIds = sharedPreferences.getString("contact_ids", "contact1,contact2"); // Fallback to legacy
            if (!currentIds.isEmpty()) {
                String[] idArray = currentIds.split(",");
                for (String contactId : idArray) {
                    if (contactId.isEmpty()) continue;
                    String name = sharedPreferences.getString(contactId + "_name", "");
                    String phone = sharedPreferences.getString(contactId + "_phone", "");
                    String relationship = sharedPreferences.getString(contactId + "_relationship", "Other");
                    boolean isPrimary = sharedPreferences.getBoolean(contactId + "_isPrimary", false);
                    
                    if (phone.isEmpty() && (contactId.equals("contact1") || contactId.equals("contact2"))) continue;
                    
                    contacts.add(new Contact(contactId, name, phone, relationship, isPrimary));
                }
            }
        }
        
        // Sort: Primary first
        contacts.sort((c1, c2) -> {
            if (c1.isPrimary && !c2.isPrimary) return -1;
            if (!c1.isPrimary && c2.isPrimary) return 1;
            return 0;
        });
        
        return contacts;
    }

    private void loadLocalContacts() {
        contactsLiveData.setValue(getLocalContactsSync());
    }

    public void syncWithFirestore() {
        if (authRepo.isDemoUser()) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        isSyncing.setValue(true);
        db.collection("users").document(user.getUid()).collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (sharedPreferences != null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        StringBuilder ids = new StringBuilder();
                        queryDocumentSnapshots.forEach(doc -> {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String phone = doc.getString("phone");
                            String relationship = doc.getString("relationship");
                            if (relationship == null) relationship = "Other";
                            Boolean isPrimaryObj = doc.getBoolean("isPrimary");
                            boolean isPrimary = isPrimaryObj != null && isPrimaryObj;
                            
                            if (ids.length() > 0) ids.append(",");
                            ids.append(id);
                            
                            editor.putString(id + "_name", name);
                            editor.putString(id + "_phone", phone);
                            editor.putString(id + "_relationship", relationship);
                            editor.putBoolean(id + "_isPrimary", isPrimary);
                        });
                        editor.putString("contact_ids", ids.toString());
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
        public String relationship;
        public boolean isPrimary;

        public Contact() {}

        public Contact(String id, String name, String phone, String relationship, boolean isPrimary) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.relationship = relationship;
            this.isPrimary = isPrimary;
        }
    }
}
