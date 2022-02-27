package com.trss.bi.domain;

public enum ApplicationAssignmentStatus {

    HIDDEN("HIDDEN"),
    UNAVAILABLE("UNAVAILABLE"),
    AVAILABLE("AVAILABLE");

    private final String code;

    ApplicationAssignmentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
