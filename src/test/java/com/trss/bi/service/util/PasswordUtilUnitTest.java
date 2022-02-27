package com.trss.bi.service.util;

import com.trss.bi.web.rest.errors.InvalidPasswordException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class PasswordUtilUnitTest {

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordLengthShort() {
        // min length is 10 chars
        String password = "To0Short!";
        PasswordUtil.validatePassword(password);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordLengthLong() {
        // max length is 100 chars
        String password = "To0 Long!" + RandomStringUtils.randomAlphanumeric(100);
        PasswordUtil.validatePassword(password);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordUpperCase() {
        String password = "this password is missing an upper case letter!2";
        PasswordUtil.validatePassword(password);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordLowerCase() {
        String password = "THIS PASSWORD IS MISSING A LOWER CASE LETTER!2";
        PasswordUtil.validatePassword(password);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordNumber() {
        String password = "This password is missing a number!";
        PasswordUtil.validatePassword(password);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testPasswordSpecialChar() {
        String password = "This password is missing a special char2";
        PasswordUtil.validatePassword(password);
    }

    @Test
    public void testPasswordValid() {
        String password = "This password is valid!2";
        PasswordUtil.validatePassword(password);

        password = "$ Starts with a special character2";
        PasswordUtil.validatePassword(password);

        password = " Starts with a space!2";
        PasswordUtil.validatePassword(password);

        password = "2 Starts with a number!";
        PasswordUtil.validatePassword(password);
    }
}
