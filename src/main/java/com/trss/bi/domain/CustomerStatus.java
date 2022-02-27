package com.trss.bi.domain;

public enum CustomerStatus {

    INACTIVE("INACTIVE"),
    SETUP("SETUP"),
    ACTIVE("ACTIVE");

    private final String code;

    CustomerStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
