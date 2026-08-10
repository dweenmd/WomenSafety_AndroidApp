package com.dweenmd.womensafety.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnContactActionListener {

    private ContactsViewModel viewModel;
    private ContactsAdapter adapter;
    private TextView tvStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        RecyclerView rvContacts = view.findViewById(R.id.rv_contacts);
        ProgressBar progressSync = view.findViewById(R.id.progress_sync);
        tvStatus = view.findViewById(R.id.tv_contacts_status);
        View btnAddContact = view.findViewById(R.id.btn_add_contact);

        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(this);
        rvContacts.setAdapter(adapter);

        viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.setContacts(contacts);
            int count = contacts.size();
            tvStatus.setText(count + " contact" + (count == 1 ? "" : "s") + " available");
        });

        viewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            progressSync.setVisibility(isSyncing ? View.VISIBLE : View.INVISIBLE);
        });

        btnAddContact.setOnClickListener(v -> showContactForm(null));
    }

    private void showContactForm(ContactsRepository.Contact contact) {
        ContactFormBottomSheet bottomSheet = ContactFormBottomSheet.newInstance(contact, newContact -> {
            viewModel.saveContact(newContact);
            Toast.makeText(getContext(), "Contact saved successfully", Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getParentFragmentManager(), "ContactForm");
    }

    @Override
    public void onCall(ContactsRepository.Contact contact) {
        if (contact.phone == null || contact.phone.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + contact.phone));
        startActivity(intent);
    }

    @Override
    public void onMessage(ContactsRepository.Contact contact) {
        if (contact.phone == null || contact.phone.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + contact.phone));
        startActivity(intent);
    }

    @Override
    public void onEdit(ContactsRepository.Contact contact) {
        showContactForm(contact);
    }

    @Override
    public void onSetPrimary(ContactsRepository.Contact contact) {
        contact.isPrimary = true;
        viewModel.saveContact(contact);
        Toast.makeText(getContext(), contact.name + " set as Primary", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(ContactsRepository.Contact contact) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this emergency contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteContact(contact.id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
