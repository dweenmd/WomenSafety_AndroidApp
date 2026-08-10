package com.dweenmd.womensafety.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final GoogleSignInClient mGoogleSignInClient;
    private final MutableLiveData<FirebaseUser> currentUserLiveData;

    public AuthRepository(Context context, String webClientId) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        
        currentUserLiveData = new MutableLiveData<>(mAuth.getCurrentUser());
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUserLiveData;
    }

    public Intent getSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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
        if (user == null) return;
        
        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("email", user.getEmail());
                        profile.put("displayName", user.getDisplayName());
                        profile.put("createdAt", System.currentTimeMillis());
                        db.collection("users").document(uid).set(profile)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to create profile", e));
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            currentUserLiveData.setValue(null);
        });
    }

    public void deleteAccount(AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid).delete().addOnCompleteListener(task -> {
                user.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        currentUserLiveData.setValue(null);
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(deleteTask.getException());
                    }
                });
            });
        } else {
            callback.onFailure(new Exception("No user signed in"));
        }
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
