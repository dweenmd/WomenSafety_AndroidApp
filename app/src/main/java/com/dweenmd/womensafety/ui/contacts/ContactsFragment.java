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
        View cardEmptyTrusted = view.findViewById(R.id.card_empty_trusted);
        View btnAddContactTop = view.findViewById(R.id.btn_add_contact_top);
        View btnAddTrustedContact = view.findViewById(R.id.btn_add_trusted_contact);
        View btnSearch = view.findViewById(R.id.btn_search);

        android.widget.ImageButton btnMenu = view.findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (requireActivity() instanceof com.dweenmd.womensafety.ui.MainActivity) {
                    ((com.dweenmd.womensafety.ui.MainActivity) requireActivity()).openDrawer();
                }
            });
        }

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Search not implemented yet", Toast.LENGTH_SHORT).show();
            });
        }

        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(this);
        rvContacts.setAdapter(adapter);

        viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.setContacts(contacts);
            if (contacts.isEmpty()) {
                rvContacts.setVisibility(View.GONE);
                cardEmptyTrusted.setVisibility(View.VISIBLE);
            } else {
                rvContacts.setVisibility(View.VISIBLE);
                cardEmptyTrusted.setVisibility(View.GONE);
            }
        });

        viewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            progressSync.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
        });

        View.OnClickListener addContactListener = v -> showContactForm(null);
        if (btnAddContactTop != null) btnAddContactTop.setOnClickListener(addContactListener);
        if (btnAddTrustedContact != null) btnAddTrustedContact.setOnClickListener(addContactListener);
    }

    private void showContactForm(ContactsRepository.Contact contact) {
        ContactFormBottomSheet bottomSheet = ContactFormBottomSheet.newInstance(contact, newContact -> {
            viewModel.saveContact(newContact);
            Toast.makeText(getContext(), R.string.toast_contact_saved, Toast.LENGTH_SHORT).show();
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
    public void onShareWhatsApp(ContactsRepository.Contact contact) {
        if (contact.phone == null || contact.phone.isEmpty()) return;
        com.dweenmd.womensafety.sos.LiveSessionManager manager =
                new com.dweenmd.womensafety.sos.LiveSessionManager(requireContext());

        if (com.dweenmd.womensafety.sos.LiveSessionManager.isSharing(requireContext())) {
            manager.shareViaWhatsApp(contact.phone,
                    manager.buildShareUrl(requireContext()
                            .getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
                            .getString(com.dweenmd.womensafety.service.LiveLocationService.PREF_SESSION_ID, "")));
        } else {
            // No live session: send the current location as a one-time map link
            new com.dweenmd.womensafety.data.LocationRepository(requireContext())
                    .getCurrentLocation(new com.dweenmd.womensafety.data.LocationRepository.LocationCallbackResult() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (!isAdded()) return;
                            String url = "https://www.google.com/maps/search/?api=1&query="
                                    + location.getLatitude() + "," + location.getLongitude();
                            String e164 = contact.phone.replaceAll("[^0-9]", "");
                            String msg = "My current location: " + url;
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/" + e164 + "?text=" + Uri.encode(msg))));
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String reason) {
                            if (isAdded()) Toast.makeText(requireContext(), "Could not get location: " + reason, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
