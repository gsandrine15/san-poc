package com.trss.bi.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InvalidPasswordException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST);
    }

    public InvalidPasswordException(String message) {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, message, Status.BAD_REQUEST);
    }
}
