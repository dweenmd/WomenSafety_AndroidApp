package com.dweenmd.womensafety.ui.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.data.ContactsRepository;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PRIMARY = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private final List<ContactsRepository.Contact> contacts = new ArrayList<>();
    private final OnContactActionListener listener;

    public interface OnContactActionListener {
        void onCall(ContactsRepository.Contact contact);
        void onMessage(ContactsRepository.Contact contact);
        void onEdit(ContactsRepository.Contact contact);
        void onSetPrimary(ContactsRepository.Contact contact);
        void onDelete(ContactsRepository.Contact contact);
    }

    public ContactsAdapter(OnContactActionListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<ContactsRepository.Contact> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        notifyDataSetChanged();
    }
    
    public List<ContactsRepository.Contact> getContacts() {
        return contacts;
    }

    @Override
    public int getItemViewType(int position) {
        return contacts.get(position).isPrimary ? VIEW_TYPE_PRIMARY : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PRIMARY) {
            return new PrimaryContactViewHolder(inflater.inflate(R.layout.item_contact_primary, parent, false));
        } else {
            return new OtherContactViewHolder(inflater.inflate(R.layout.item_contact, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactsRepository.Contact contact = contacts.get(position);
        if (holder instanceof PrimaryContactViewHolder) {
            ((PrimaryContactViewHolder) holder).bind(contact);
        } else if (holder instanceof OtherContactViewHolder) {
            ((OtherContactViewHolder) holder).bind(contact);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void showPopupMenu(View anchor, ContactsRepository.Contact contact) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Edit Contact");
        if (!contact.isPrimary) {
            popup.getMenu().add(0, 2, 0, "Set as Primary");
        }
        popup.getMenu().add(0, 3, 0, "Delete");
        
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    listener.onEdit(contact);
                    return true;
                case 2:
                    listener.onSetPrimary(contact);
                    return true;
                case 3:
                    listener.onDelete(contact);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    class PrimaryContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRelationship, tvPhone;
        View btnCall, btnMessage, btnMenu;

        PrimaryContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_primary);
            tvRelationship = itemView.findViewById(R.id.tv_relationship_primary);
            tvPhone = itemView.findViewById(R.id.tv_phone_primary);
            btnCall = itemView.findViewById(R.id.btn_call_primary);
            btnMessage = itemView.findViewById(R.id.btn_message_primary);
            btnMenu = itemView.findViewById(R.id.btn_menu_primary);
        }

        void bind(ContactsRepository.Contact contact) {
            tvName.setText(contact.name);
            tvRelationship.setText(contact.relationship);
            tvPhone.setText(contact.phone);

            btnCall.setOnClickListener(v -> listener.onCall(contact));
            btnMessage.setOnClickListener(v -> listener.onMessage(contact));
            btnMenu.setOnClickListener(v -> showPopupMenu(v, contact));
        }
    }

    class OtherContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRelationship, tvPhone;
        View btnCall, btnMenu;

        OtherContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_other);
            tvRelationship = itemView.findViewById(R.id.tv_relationship_other);
            tvPhone = itemView.findViewById(R.id.tv_phone_other);
            btnCall = itemView.findViewById(R.id.btn_call_other);
            btnMenu = itemView.findViewById(R.id.btn_menu_other);
        }

        void bind(ContactsRepository.Contact contact) {
            tvName.setText(contact.name);
            tvRelationship.setText(contact.relationship);
            tvPhone.setText(contact.phone);

            btnCall.setOnClickListener(v -> listener.onCall(contact));
            btnMenu.setOnClickListener(v -> showPopupMenu(v, contact));
        }
    }
}
