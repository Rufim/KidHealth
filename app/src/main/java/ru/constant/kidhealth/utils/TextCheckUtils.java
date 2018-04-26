package ru.constant.kidhealth.utils;

public class TextCheckUtils {

    public static final boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }
}
