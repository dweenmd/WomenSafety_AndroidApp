package com.dweenmd.womensafety.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;
import com.dweenmd.womensafety.data.PhoneNumberValidator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ContactsFragment extends Fragment {

    private ContactsViewModel viewModel;
    private TextInputEditText etContact1Name, etContact1Phone;
    private TextInputEditText etContact2Name, etContact2Phone;
    private ProgressBar progressSync;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        etContact1Name = view.findViewById(R.id.et_contact1_name);
        etContact1Phone = view.findViewById(R.id.et_contact1_phone);
        etContact2Name = view.findViewById(R.id.et_contact2_name);
        etContact2Phone = view.findViewById(R.id.et_contact2_phone);
        progressSync = view.findViewById(R.id.progress_sync);

        view.findViewById(R.id.btn_save_contact1).setOnClickListener(v -> saveContact(1, etContact1Name, etContact1Phone));
        view.findViewById(R.id.btn_save_contact2).setOnClickListener(v -> saveContact(2, etContact2Name, etContact2Phone));

        viewModel.getContacts().observe(getViewLifecycleOwner(), this::populateContacts);
        viewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> 
                progressSync.setVisibility(isSyncing ? View.VISIBLE : View.INVISIBLE)
        );
    }

    private void populateContacts(List<ContactsRepository.Contact> contacts) {
        for (ContactsRepository.Contact contact : contacts) {
            if ("contact1".equals(contact.id)) {
                etContact1Name.setText(contact.name);
                etContact1Phone.setText(contact.phone);
            } else if ("contact2".equals(contact.id)) {
                etContact2Name.setText(contact.name);
                etContact2Phone.setText(contact.phone);
            }
        }
    }

    private void saveContact(int index, TextInputEditText etName, TextInputEditText etPhone) {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both name and phone", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PhoneNumberValidator.isValid(phone)) {
            etPhone.setError("Invalid BD phone number (e.g. 01XXXXXXXXX)");
            return;
        }

        viewModel.saveContact(index, name, phone);
        Toast.makeText(requireContext(), "Contact " + index + " saved", Toast.LENGTH_SHORT).show();
    }
}
