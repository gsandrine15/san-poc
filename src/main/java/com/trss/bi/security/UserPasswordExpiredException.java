package com.trss.bi.security;

import org.springframework.security.core.AuthenticationException;

public class UserPasswordExpiredException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public UserPasswordExpiredException(String message) {
        super(message);
    }

    public UserPasswordExpiredException(String message, Throwable t) {
        super(message, t);
    }
}
