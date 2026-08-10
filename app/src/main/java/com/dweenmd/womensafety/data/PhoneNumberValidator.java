package com.dweenmd.womensafety.data;

import android.text.TextUtils;

public class PhoneNumberValidator {

    /**
     * Validates a phone number.
     * Currently implemented as a simple 11-digit regex for BD numbers,
     * as requested in the plan (wrap current 11-digit regex now, pull in libphonenumber later if needed).
     */
    public static boolean isValid(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        
        // simple validation for bd 11 digit numbers starting with 01
        String regex = "^01[3-9]\\d{8}$";
        return phoneNumber.matches(regex);
    }
}
