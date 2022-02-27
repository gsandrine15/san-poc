package com.trss.bi.service.util;

import com.trss.bi.web.rest.errors.InvalidPasswordException;

import java.util.regex.Pattern;

public class PasswordUtil {
    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD_RULES_PATTERN_STRING = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{10,100}$";

    private static boolean isPasswordValid(String password) {
        return Pattern.matches(PASSWORD_RULES_PATTERN_STRING, password);
    }

    public static void validatePassword(String password) {
        if (password == null || !isPasswordValid(password)) {
            throw new InvalidPasswordException("New password does not meet complexity requirements.");
        }
    }
}
