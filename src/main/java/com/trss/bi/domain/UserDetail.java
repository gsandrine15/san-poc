package com.trss.bi.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A Customer.
 */
@Entity
@Table(name = "user_detail")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserDetail extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_date")
    private Instant passwordDate = Instant.now();

    @Column(nullable = false)
    private boolean deleted = false;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
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
        this.customer=customer;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Instant getPasswordDate() {
        return passwordDate;
    }

    public void setPasswordDate(Instant passwordDate) {
        this.passwordDate = passwordDate;
    }

    public boolean getDeleted() { return deleted; }

    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDetail userDetail = (UserDetail) o;
        if (userDetail.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userDetail.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Customer{" +
            "id=" + getId() +
            ", customer='" + getCustomer().toString() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", passwordDate='" + getPasswordDate() + "'" +
            ", deleted='" + getDeleted() + "'" +
            "}";
    }
}
