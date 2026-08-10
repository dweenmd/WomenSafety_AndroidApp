package com.dweenmd.womensafety.ui.auth;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.dweenmd.womensafety.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public AuthViewModel(Context context) {
        authRepository = new AuthRepository(context);
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return authRepository.getCurrentUser();
    }
}
