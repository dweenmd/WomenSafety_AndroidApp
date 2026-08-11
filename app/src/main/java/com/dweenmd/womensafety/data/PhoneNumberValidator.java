package com.dweenmd.womensafety.data;

import android.text.TextUtils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.Locale;

public class PhoneNumberValidator {

    /**
     * Validates a phone number using libphonenumber.
     */
    public static boolean isValid(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            String defaultRegion = Locale.getDefault().getCountry().toUpperCase(Locale.US);
            if (defaultRegion.isEmpty()) {
                defaultRegion = "US"; // Fallback
            }
            
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, defaultRegion);
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            // If it fails parsing, fallback to basic length check to not break completely
            // if users enter local shortcodes or weirdly formatted numbers that libphonenumber rejects.
            String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
            return cleaned.matches("^\\+?[0-9]{3,15}$");
        }
    }
}
