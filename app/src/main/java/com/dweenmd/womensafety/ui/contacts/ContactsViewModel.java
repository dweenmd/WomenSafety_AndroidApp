package com.dweenmd.womensafety.ui.contacts;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.dweenmd.womensafety.data.ContactsRepository;
import java.util.List;

public class ContactsViewModel extends AndroidViewModel {

    private final ContactsRepository repository;

    public ContactsViewModel(Application application) {
        super(application);
        this.repository = new ContactsRepository(application);
    }

    public LiveData<List<ContactsRepository.Contact>> getContacts() {
        return repository.getContacts();
    }
    
    public LiveData<Boolean> getIsSyncing() {
        return repository.getIsSyncing();
    }

    public void saveContact(ContactsRepository.Contact contact) {
        repository.saveContact(contact);
    }

    public void deleteContact(String contactId) {
        repository.deleteContact(contactId);
    }
}
