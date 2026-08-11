package com.dweenmd.womensafety.ui.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class ContactFormBottomSheet extends BottomSheetDialogFragment {

    private ContactsRepository.Contact contactToEdit;
    private OnContactSavedListener listener;

    private TextInputEditText etName;
    private TextInputEditText etPhone;

    private final ActivityResultLauncher<Intent> pickContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    processContactData(result.getData().getData());
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchContactPicker();
                } else {
                    Toast.makeText(getContext(), R.string.toast_permission_denied_contacts, Toast.LENGTH_SHORT).show();
                }
            }
    );

    public interface OnContactSavedListener {
        void onContactSaved(ContactsRepository.Contact contact);
    }

    public static ContactFormBottomSheet newInstance(ContactsRepository.Contact contact, OnContactSavedListener listener) {
        ContactFormBottomSheet fragment = new ContactFormBottomSheet();
        fragment.contactToEdit = contact;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_contact_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_form_title);
        etName = view.findViewById(R.id.et_form_name);
        etPhone = view.findViewById(R.id.et_form_phone);
        Spinner spinnerRelationship = view.findViewById(R.id.spinner_relationship);
        RadioButton rbPrimary = view.findViewById(R.id.rb_primary);
        RadioButton rbSecondary = view.findViewById(R.id.rb_secondary);
        View btnSave = view.findViewById(R.id.btn_save_contact);
        View btnPickContact = view.findViewById(R.id.btn_pick_contact);

        if (contactToEdit != null) {
            tvTitle.setText(R.string.contact_form_edit_title);
            etName.setText(contactToEdit.name);
            etPhone.setText(contactToEdit.phone);
            
            if (contactToEdit.isPrimary) {
                rbPrimary.setChecked(true);
            } else {
                rbSecondary.setChecked(true);
            }

            if (contactToEdit.relationship != null) {
                @SuppressWarnings("unchecked")
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerRelationship.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(contactToEdit.relationship);
                    if (position >= 0) spinnerRelationship.setSelection(position);
                }
            }
        }

        btnPickContact.setOnClickListener(v -> checkPermissionAndPickContact());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String relationship = spinnerRelationship.getSelectedItem().toString();
            boolean isPrimary = rbPrimary.isChecked();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(getContext(), R.string.toast_name_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!com.dweenmd.womensafety.data.PhoneNumberValidator.isValid(phone)) {
                Toast.makeText(getContext(), "Invalid phone number format", Toast.LENGTH_SHORT).show();
                return;
            }

            ContactsRepository.Contact newContact = new ContactsRepository.Contact(
                    contactToEdit != null ? contactToEdit.id : null,
                    name,
                    phone,
                    relationship,
                    isPrimary
            );

            if (listener != null) {
                listener.onContactSaved(newContact);
            }
            dismiss();
        });
    }

    private void checkPermissionAndPickContact() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            launchContactPicker();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        pickContactLauncher.launch(intent);
    }

    private void processContactData(Uri contactDataUri) {
        if (contactDataUri == null) return;
        
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        try (Cursor cursor = requireContext().getContentResolver().query(contactDataUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                
                if (nameIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    etName.setText(name);
                }
                if (phoneIndex != -1) {
                    String phone = cursor.getString(phoneIndex);
                    etPhone.setText(phone);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.toast_failed_read_contact, Toast.LENGTH_SHORT).show();
        }
    }
}
