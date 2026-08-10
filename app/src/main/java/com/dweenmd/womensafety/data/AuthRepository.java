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
    private final com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;

    public AuthRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.dweenmd.womensafety.R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso);
        
        currentUserLiveData = new MutableLiveData<>(mAuth.getCurrentUser());
    }

    public Intent getSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
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
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            currentUserLiveData.setValue(null);
        });
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
