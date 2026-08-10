package com.dweenmd.womensafety.ui.profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dweenmd.womensafety.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final FirebaseFirestore db;
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> phoneLiveData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        db = FirebaseFirestore.getInstance();
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        if (authRepository.isDemoUser()) {
            nameLiveData.setValue("Demo User");
            emailLiveData.setValue("demo@app.com");
            phoneLiveData.setValue("+1234567890");
            return;
        }

        FirebaseUser user = authRepository.getCurrentUser().getValue();
        if (user != null) {
            String uid = user.getUid();
            
            // Set defaults from FirebaseUser
            emailLiveData.setValue(user.getEmail() != null ? user.getEmail() : "No Email");
            phoneLiveData.setValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "No Phone");
            nameLiveData.setValue(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");

            // Fetch extra details from Firestore
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("name")) {
                                nameLiveData.setValue(documentSnapshot.getString("name"));
                            }
                            if (documentSnapshot.contains("email")) {
                                emailLiveData.setValue(documentSnapshot.getString("email"));
                            }
                            if (documentSnapshot.contains("phone")) {
                                phoneLiveData.setValue(documentSnapshot.getString("phone"));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ProfileViewModel", "Failed to load profile", e));
        }
    }

    public LiveData<String> getName() {
        return nameLiveData;
    }

    public LiveData<String> getEmail() {
        return emailLiveData;
    }

    public LiveData<String> getPhone() {
        return phoneLiveData;
    }
}
