package com.trss.bi.security;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case of a user for an inactive customer trying to authenticate.
 */
public class UserInactiveCustomerException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public UserInactiveCustomerException(String message) {
        super(message);
    }

    public UserInactiveCustomerException(String message, Throwable t) {
        super(message, t);
    }
}
