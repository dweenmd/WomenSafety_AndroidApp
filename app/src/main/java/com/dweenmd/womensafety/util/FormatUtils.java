package com.dweenmd.womensafety.util;

/** Display-formatting helpers shared across the profile UI. */
public final class FormatUtils {

    private FormatUtils() {}

    /** Masks an email like "jane.doe@gmail.com" → "j***@gmail.com". */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return email;
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    /** Masks a phone like "+8801712345678" → "+880******78". */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        String prefix = phone.substring(0, 4);
        String suffix = phone.substring(phone.length() - 2);
        return prefix + "******" + suffix;
    }
}
