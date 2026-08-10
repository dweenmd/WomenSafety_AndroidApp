package com.dweenmd.womensafety.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isServiceRunning = new MutableLiveData<>(false);
    
    public LiveData<Boolean> getIsServiceRunning() {
        return isServiceRunning;
    }

    public void setServiceRunning(boolean running) {
        isServiceRunning.setValue(running);
    }
}
