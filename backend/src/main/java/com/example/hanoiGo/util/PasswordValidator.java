package com.example.hanoiGo.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final String PASSWORD_PATTERN =
        "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValid(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public static String getValidationMessage() {
        return "Password phải có ít nhất 8 ký tự, bao gồm chữ cái, số và ký tự đặc biệt (@$!%*?&)";
    }

    public static void validatePassword(String password) throws IllegalArgumentException {
        if (!isValid(password)) {
            throw new IllegalArgumentException(getValidationMessage());
        }
    }
}