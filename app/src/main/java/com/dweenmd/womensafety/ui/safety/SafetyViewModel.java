package com.dweenmd.womensafety.ui.safety;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class SafetyViewModel extends ViewModel {

    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final MutableLiveData<Boolean> backgroundProtection = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> liveLocationOnSos = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> shakeDetection = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> autoNotify = new MutableLiveData<>(false);

    public SafetyViewModel(Context context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        loadLocalSettings();
        syncWithFirestore();
    }

    private void loadLocalSettings() {
        backgroundProtection.setValue(prefs.getBoolean("backgroundProtection", true));
        liveLocationOnSos.setValue(prefs.getBoolean("liveLocationOnSos", true));
        shakeDetection.setValue(prefs.getBoolean("shakeDetection", true));
        autoNotify.setValue(prefs.getBoolean("autoNotify", false));
    }

    private void syncWithFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).collection("settings").document("preferences")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        if (documentSnapshot.contains("backgroundProtection")) editor.putBoolean("backgroundProtection", documentSnapshot.getBoolean("backgroundProtection"));
                        if (documentSnapshot.contains("liveLocationOnSos")) editor.putBoolean("liveLocationOnSos", documentSnapshot.getBoolean("liveLocationOnSos"));
                        if (documentSnapshot.contains("shakeDetection")) editor.putBoolean("shakeDetection", documentSnapshot.getBoolean("shakeDetection"));
                        if (documentSnapshot.contains("autoNotify")) editor.putBoolean("autoNotify", documentSnapshot.getBoolean("autoNotify"));
                        editor.apply();
                        loadLocalSettings();
                    }
                });
    }

    public void setSetting(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        loadLocalSettings();
        
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> update = new HashMap<>();
            update.put(key, value);
            db.collection("users").document(user.getUid()).collection("settings").document("preferences")
                    .set(update, SetOptions.merge());
        }
    }

    public LiveData<Boolean> getBackgroundProtection() { return backgroundProtection; }
    public LiveData<Boolean> getLiveLocationOnSos() { return liveLocationOnSos; }
    public LiveData<Boolean> getShakeDetection() { return shakeDetection; }
    public LiveData<Boolean> getAutoNotify() { return autoNotify; }
}
