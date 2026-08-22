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

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private final List<ContactsRepository.Contact> contacts = new ArrayList<>();
    private final OnContactActionListener listener;

    public interface OnContactActionListener {
        void onCall(ContactsRepository.Contact contact);
        void onMessage(ContactsRepository.Contact contact);
        void onShareWhatsApp(ContactsRepository.Contact contact);
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

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ContactViewHolder(inflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactsRepository.Contact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void showPopupMenu(View anchor, ContactsRepository.Contact contact) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Edit Contact");
        popup.getMenu().add(0, 4, 0, "Share Location via WhatsApp");
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
                case 4:
                    listener.onShareWhatsApp(contact);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSubtitle, tvInitials;
        View btnCall, btnMessage, btnMenu, ivAvatar, vAvatarBg;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_other);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle_other);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            vAvatarBg = itemView.findViewById(R.id.v_avatar_bg);
            btnCall = itemView.findViewById(R.id.btn_call_other);
            btnMessage = itemView.findViewById(R.id.btn_message_other);
            btnMenu = itemView.findViewById(R.id.btn_menu_other);
        }

        void bind(ContactsRepository.Contact contact) {
            tvName.setText(contact.name);
            tvSubtitle.setText(contact.relationship + " • " + contact.phone);
            
            if (contact.name != null && contact.name.length() > 0) {
                tvInitials.setText(contact.name.substring(0, 1).toUpperCase());
                tvInitials.setVisibility(View.VISIBLE);
                vAvatarBg.setVisibility(View.VISIBLE);
                ivAvatar.setVisibility(View.GONE);
            } else {
                tvInitials.setVisibility(View.GONE);
                vAvatarBg.setVisibility(View.GONE);
                ivAvatar.setVisibility(View.VISIBLE);
            }

            if (contact.isPrimary) {
                tvName.setText(contact.name + " (Primary)");
            }

            btnCall.setOnClickListener(v -> listener.onCall(contact));
            btnMessage.setOnClickListener(v -> listener.onMessage(contact));
            btnMenu.setOnClickListener(v -> showPopupMenu(v, contact));
        }
    }
}
