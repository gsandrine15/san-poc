package com.trss.bi.service.dto;

import com.trss.bi.domain.ApplicationAssignmentStatus;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the CustomerApplication entity.
 */
public class CustomerApplicationDTO implements Serializable {

    private Long id;

    @NotNull
    private ApplicationAssignmentStatus status;

    private Long customerId;

    private String customerName;

    private Long applicationId;

    private String applicationName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationAssignmentStatus status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerApplicationDTO customerApplicationDTO = (CustomerApplicationDTO) o;
        if (customerApplicationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), customerApplicationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CustomerApplicationDTO{" +
            "id=" + getId() +
            ", status=" + getStatus() +
            ", customer=" + getCustomerId() +
            ", customer='" + getCustomerName() + "'" +
            ", application=" + getApplicationId() +
            ", application='" + getApplicationName() + "'" +
            "}";
    }
}
