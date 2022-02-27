package com.trss.bi.service.dto;

import java.io.Serializable;

public class AuthorityDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AuthorityDTO{" +
            "name='" + name + '\'' +
            "}";
    }
}
