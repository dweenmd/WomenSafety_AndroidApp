package com.dweenmd.womensafety.ui.contacts;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.dweenmd.womensafety.data.ContactsRepository;
import java.util.List;

public class ContactsViewModel extends ViewModel {

    private final ContactsRepository repository;

    public ContactsViewModel(Context context) {
        this.repository = new ContactsRepository(context);
    }

    public LiveData<List<ContactsRepository.Contact>> getContacts() {
        return repository.getContacts();
    }
    
    public LiveData<Boolean> getIsSyncing() {
        return repository.getIsSyncing();
    }

    public void saveContact(int index, String name, String phone) {
        repository.saveContact(index, name, phone);
    }
}
