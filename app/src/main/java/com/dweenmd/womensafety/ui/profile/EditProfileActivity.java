package com.dweenmd.womensafety.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextInputEditText etName, etPhone, etEmail, etDob;
    private android.widget.AutoCompleteTextView etGender;
    private View btnSave;
    private ProgressBar progressSave;

    private AuthRepository authRepository;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        authRepository = new AuthRepository(this);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ivAvatar = findViewById(R.id.iv_edit_profile_avatar);
        etName = findViewById(R.id.et_edit_name);
        etPhone = findViewById(R.id.et_edit_phone);
        etEmail = findViewById(R.id.et_edit_email);
        etDob = findViewById(R.id.et_edit_dob);
        etGender = findViewById(R.id.et_edit_gender);
        btnSave = findViewById(R.id.btn_save_profile);
        progressSave = findViewById(R.id.progress_save);

        String[] genders = {"Female", "Male", "Other", "Prefer not to say"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        etGender.setAdapter(adapter);

        loadUserData();

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        if (authRepository.isDemoUser()) {
            etName.setText("Demo User");
            etEmail.setText("demo@app.com");
            etPhone.setText("+1234567890");
            return;
        }

        FirebaseUser user = authRepository.getCurrentUser().getValue();
        if (user != null) {
            etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            etName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
            etPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(ivAvatar);
            }

            // Fetch latest details from Firestore
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            if (doc.contains("phone") && etPhone.getText().toString().isEmpty()) {
                                etPhone.setText(doc.getString("phone"));
                            }
                            if (doc.contains("dob")) {
                                etDob.setText(doc.getString("dob"));
                            }
                            if (doc.contains("gender")) {
                                etGender.setText(doc.getString("gender"), false);
                            }
                        }
                    });
        }
    }

    private void saveProfile() {
        if (authRepository.isDemoUser()) {
            Toast.makeText(this, "Cannot edit demo user", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newDob = etDob.getText().toString().trim();
        String newGender = etGender.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Name cannot be empty");
            return;
        }

        FirebaseUser user = authRepository.getCurrentUser().getValue();
        if (user == null) return;

        setLoading(true);

        if (selectedImageUri != null) {
            // Upload image first
            StorageReference profileRef = storage.getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("profile.jpg");

            profileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateAuthAndFirestore(user, newName, newPhone, newDob, newGender, uri);
                        }).addOnFailureListener(e -> {
                            setLoading(false);
                            Toast.makeText(this, "Failed to get image url: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateAuthAndFirestore(user, newName, newPhone, newDob, newGender, user.getPhotoUrl());
        }
    }

    private void updateAuthAndFirestore(FirebaseUser user, String newName, String newPhone, String dob, String gender, Uri photoUri) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);
        if (photoUri != null) {
            builder.setPhotoUri(photoUri);
        }

        user.updateProfile(builder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Firestore
                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                updates.put("name", newName);
                updates.put("phone", newPhone);
                updates.put("dob", dob);
                updates.put("gender", gender);
                if (photoUri != null) updates.put("photoUrl", photoUri.toString());
                
                db.collection("users").document(user.getUid())
                        .set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            setLoading(false);
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                setLoading(false);
                Toast.makeText(this, "Failed to update profile: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnSave.setEnabled(!isLoading);
        progressSave.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
