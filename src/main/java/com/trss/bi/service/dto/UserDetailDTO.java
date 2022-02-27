package com.trss.bi.service.dto;

import com.trss.bi.domain.Customer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Customer entity.
 */
public class UserDetailDTO implements Serializable {

    private Long id;

    @NotNull
    private Customer customer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDetailDTO userDetailDTO = (UserDetailDTO) o;
        if (userDetailDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userDetailDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
            "id=" + getId() +
            ", name='" + getCustomer().toString() + "'" +
            "}";
    }
}
