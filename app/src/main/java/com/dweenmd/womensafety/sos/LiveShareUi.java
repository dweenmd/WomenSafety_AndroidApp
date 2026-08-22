package com.dweenmd.womensafety.sos;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dweenmd.womensafety.data.ContactsRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared UI flow for starting/stopping a live-location session and pushing the
 * tracking link through SMS + WhatsApp + other apps. Used by the Home and
 * Safety screens.
 */
public final class LiveShareUi {

    private LiveShareUi() {}

    public static void handle(Fragment fragment) {
        if (!fragment.isAdded()) return;
        android.content.Context context = fragment.requireContext();

        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(context,
                    "Live tracking needs a logged-in account — sending one-time location instead",
                    Toast.LENGTH_LONG).show();
            new SosMessenger(context).shareLocationOnly(new SosMessenger.SosCallback() {
                @Override
                public void onSosTriggered(String status) {
                    if (fragment.isAdded()) Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    if (fragment.isAdded()) Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        LiveSessionManager manager = new LiveSessionManager(context);

        if (LiveSessionManager.isSharing(context)) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Live sharing is running")
                    .setMessage("Your contacts can currently track you in real time. Stop sharing now?")
                    .setPositiveButton("Stop sharing", (d, w) -> {
                        manager.stopLiveSession();
                        Toast.makeText(context, "Live sharing stopped", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }

        manager.startLiveSession(new LiveSessionManager.LiveSessionCallback() {
            @Override
            public void onStarted(String sessionId, String shareUrl, int smsSentCount) {
                if (!fragment.isAdded()) return;
                String smsStatus = smsSentCount > 0
                        ? "Link sent by SMS to " + smsSentCount + " contact(s)."
                        : "SMS failed — please share the link manually below.";
                showShareDialog(fragment, manager, shareUrl, smsStatus);
            }

            @Override
            public void onFailure(String error) {
                if (fragment.isAdded()) Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void showShareDialog(Fragment fragment, LiveSessionManager manager, String shareUrl, String smsStatus) {
        if (!fragment.isAdded()) return;
        android.content.Context context = fragment.requireContext();
        new MaterialAlertDialogBuilder(context)
                .setTitle("Live sharing started")
                .setMessage(smsStatus + "\n\nShare the tracking link through more apps:")
                .setPositiveButton("WhatsApp", (d, w) -> pickContactForWhatsApp(fragment, manager, shareUrl))
                .setNeutralButton("Other apps", (d, w) -> manager.shareViaOtherApps(shareUrl))
                .setNegativeButton("Close", null)
                .show();
    }

    private static void pickContactForWhatsApp(Fragment fragment, LiveSessionManager manager, String shareUrl) {
        if (!fragment.isAdded()) return;
        android.content.Context context = fragment.requireContext();
        List<ContactsRepository.Contact> contacts =
                new ContactsRepository(context).getLocalContactsSync();
        if (contacts.isEmpty()) {
            Toast.makeText(context, "No contacts saved", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> names = new ArrayList<>();
        for (ContactsRepository.Contact c : contacts) {
            names.add(c.name + " (" + c.phone + ")");
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle("Send via WhatsApp to")
                .setItems(names.toArray(new String[0]), (d, which) ->
                        manager.shareViaWhatsApp(contacts.get(which).phone, shareUrl))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
