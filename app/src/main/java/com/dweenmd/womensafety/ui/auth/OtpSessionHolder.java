package com.dweenmd.womensafety.ui.auth;

import com.google.firebase.auth.PhoneAuthProvider;

/**
 * Holds the in-flight phone-auth session between PhoneLoginActivity and
 * OtpVerificationActivity — ForceResendingToken cannot travel through an Intent.
 */
public class OtpSessionHolder {

    public static String phoneNumber;
    public static String verificationId;
    public static PhoneAuthProvider.ForceResendingToken resendToken;

    public static void clear() {
        phoneNumber = null;
        verificationId = null;
        resendToken = null;
    }
}
