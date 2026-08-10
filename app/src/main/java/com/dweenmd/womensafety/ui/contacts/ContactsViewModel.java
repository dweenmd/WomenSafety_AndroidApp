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

    public void saveContact(int index, String name, String phone) {
        repository.saveContact(index, name, phone);
    }
}
