package com.trss.bi.service.dto;

import com.trss.bi.domain.Authority;
import com.trss.bi.domain.Customer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;

    @NotNull
    @Size(min= 1, max = 50)
    private String name;

    private Set<Authority> authorities;

    private Customer customer;

    private Long userCount;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedDate() { return createdDate; }

    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public String getLastModifiedBy() { return lastModifiedBy; }

    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public Instant getLastModifiedDate() { return lastModifiedDate; }

    public void setLastModifiedDate(Instant lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    @Override
    public String toString(){
        return "RoleDTO{" +
            "id='" + id + "'" +
            "name='" + name + "'" +
            "authorities='" + authorities + "'" +
            "customer='" + customer + "'" +
            "userCount='" + userCount + "'" +
            "createdBy='" + createdBy + "'" +
            "createdDate='" + createdDate + "'" +
            "lastModifiedBy='" + lastModifiedBy + "'" +
            "lastModifiedDate='" + lastModifiedDate + "'" +
            "}";
    }
}
