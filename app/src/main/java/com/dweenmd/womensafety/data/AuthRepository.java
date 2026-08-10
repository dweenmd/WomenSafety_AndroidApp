package com.dweenmd.womensafety.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> currentUserLiveData;
    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        
        currentUserLiveData = new MutableLiveData<>(mAuth.getCurrentUser());
    }

    public boolean isDemoUser() {
        return prefs.getBoolean("isDemoUser", false);
    }

    public void setDemoUser(boolean isDemo) {
        prefs.edit().putBoolean("isDemoUser", isDemo).apply();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUserLiveData;
    }

    public void signInWithEmailAndPassword(String email, String password, AuthCallback callback) {
        if ("demo@app.com".equals(email) && "123456".equals(password)) {
            setDemoUser(true);
            callback.onSuccess(null); // No FirebaseUser for demo
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setDemoUser(false);
                        FirebaseUser user = mAuth.getCurrentUser();
                        currentUserLiveData.setValue(user);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void signUpWithEmailAndPassword(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setDemoUser(false);
                        FirebaseUser user = mAuth.getCurrentUser();
                        currentUserLiveData.setValue(user);
                        ensureUserProfileExists(user);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    private void ensureUserProfileExists(FirebaseUser user) {
        if (user == null || isDemoUser()) return;
        
        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("email", user.getEmail());
                        profile.put("createdAt", System.currentTimeMillis());
                        db.collection("users").document(uid).set(profile)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to create profile", e));
                    }
                });
    }

    public void signOut() {
        if (isDemoUser()) {
            setDemoUser(false);
            currentUserLiveData.setValue(null);
            return;
        }

        mAuth.signOut();
        currentUserLiveData.setValue(null);
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
