package com.trss.bi.security;

import org.springframework.security.core.AuthenticationException;

public class UserDeletedException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public UserDeletedException(String message) {
        super(message);
    }

    public UserDeletedException(String message, Throwable t) {
        super(message, t);
    }
}
