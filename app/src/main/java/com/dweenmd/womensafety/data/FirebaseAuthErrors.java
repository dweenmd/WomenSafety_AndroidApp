package com.dweenmd.womensafety.data;

/** Maps raw Firebase auth exceptions to human-readable guidance. */
public final class FirebaseAuthErrors {

    private FirebaseAuthErrors() {}

    public static String friendly(Exception e) {
        String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error";
        String code = (e instanceof com.google.firebase.auth.FirebaseAuthException)
                ? ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode() : "";

        if (code.contains("INVALID_APP_CREDENTIAL") || msg.contains("SHA-1")
                || msg.contains("App fingerprint") || msg.contains("not authorized")) {
            return "This app's SHA-1 fingerprint is not registered in the Firebase console "
                    + "(Project Settings → Your apps → Add fingerprint).";
        }
        if (code.contains("QUOTA") || msg.contains("quota")) {
            return "SMS quota exceeded for today. Try again tomorrow or use email login.";
        }
        if (code.contains("TOO_MANY_REQUESTS") || msg.contains("too many requests")) {
            return "Too many attempts. Please wait a while and try again.";
        }
        if (code.contains("INVALID_PHONE_NUMBER") || msg.contains("invalid phone")) {
            return "That phone number looks invalid. Include the right country code.";
        }
        if (code.contains("credential")) {
            return "Wrong code. Please check the 6-digit SMS code and try again.";
        }
        if (code.contains("USER_NOT_FOUND")) {
            return "No account found with this email. Please register first.";
        }
        if (code.contains("INVALID_CREDENTIAL") || code.contains("WRONG_PASSWORD")
                || msg.contains("password is invalid")) {
            return "Wrong email or password.";
        }
        if (code.contains("EMAIL_ALREADY_IN_USE") || msg.contains("already in use")) {
            return "An account already exists with this email. Please login instead.";
        }
        if (code.contains("WEAK_PASSWORD")) {
            return "Password too weak — use at least 6 characters.";
        }
        if (code.contains("NETWORK")) {
            return "Network error. Check your internet connection.";
        }
        return msg;
    }
}
