package com.trss.bi.web.rest.errors;

public class CustomerAccessException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public CustomerAccessException() {
        super(ErrorConstants.CUSTOMER_ACCESS_TYPE, "You cannot view or edit data for this customer", "customerAccess", "customerAccess");
    }
}
