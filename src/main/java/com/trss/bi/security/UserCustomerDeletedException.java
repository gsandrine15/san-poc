package com.trss.bi.security;

import org.springframework.security.core.AuthenticationException;

public class UserCustomerDeletedException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public UserCustomerDeletedException(String message) {
        super(message);
    }

    public UserCustomerDeletedException(String message, Throwable t) {
        super(message, t);
    }
}
