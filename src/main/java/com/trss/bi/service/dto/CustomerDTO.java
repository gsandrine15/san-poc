package com.trss.bi.service.dto;

import com.trss.bi.domain.CustomerStatus;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Customer entity.
 */
public class CustomerDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String name;

    @NotNull
    @Size(max = 20)
    private String number;

    @NotNull
    @Size(max = 20)
    private CustomerStatus status;

    @NotNull
    private Integer sessionTimeoutS;

    @NotNull
    private Integer passwordExpiration;

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public Integer getSessionTimeoutS() {
        return sessionTimeoutS;
    }

    public void setSessionTimeoutS(Integer sessionTimeoutS) {
        this.sessionTimeoutS = sessionTimeoutS;
    }

    public Integer getPasswordExpiration() {
        return passwordExpiration;
    }

    public void setPasswordExpiration(Integer passwordExpiration) {
        this.passwordExpiration = passwordExpiration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerDTO customerDTO = (CustomerDTO) o;
        if (customerDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), customerDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", number='" + getNumber() + "'" +
            ", status='" + getStatus() + "'" +
            ", sessionTimeoutS='" + getSessionTimeoutS() + "'" +
            ", passwordExpiration='" + getPasswordExpiration() + "'" +
            "}";
    }
}
