package com.trss.bi.domain;

public enum CustomerMarket {

    GOVERNMENT("GOVERNMENT"),
    CORPORATE("CORPORATE");

    private final String value;

    CustomerMarket(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
