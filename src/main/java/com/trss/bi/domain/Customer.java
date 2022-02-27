package com.trss.bi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * A Customer.
 */
@Entity
@Table(name = "customer")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Customer extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotNull
    @Size(max = 20)
    @Column(name = "number", length = 20, nullable = false)
    private String number;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status;

    @NotNull
    @Column(name = "session_timeout_s", nullable = false)
    private Integer sessionTimeoutS;

    @NotNull
    @Column(name = "password_expiration", nullable = false)
    private Integer passwordExpiration = 90;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false)
    private CustomerMarket market;

    @NotNull
    @Column(name = "contract_start_date", nullable = false)
    private Date contractStartDate;

    @NotNull
    @Column(name = "contract_end_date", nullable = false)
    private Date contractEndDate;

    @NotNull
    @Column(name = "bd_owner", nullable = false)
    private String bdOwner;

    @NotNull
    @Column(name = "analysts", nullable = false)
    private String analysts;

    @Column(name = "notes")
    private String notes;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private Set<Role> roles;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Customer name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public Customer number(String number) {
        this.number = number;
        return this;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Customer status(CustomerStatus status) {
        this.status = status;
        return this;
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

    public CustomerMarket getMarket() {
        return market;
    }

    public void setMarket(CustomerMarket market) {
        this.market = market;
    }

    public Date getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(Date contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getBdOwner() {
        return bdOwner;
    }

    public void setBdOwner(String bdOwner) {
        this.bdOwner = bdOwner;
    }

    public String getAnalysts() {
        return analysts;
    }

    public void setAnalysts(String analysts) {
        this.analysts = analysts;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean getDeleted() { return deleted; }

    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    @JsonProperty
    public Instant getCreatedDate() {
        return super.getCreatedDate();
    }

    @Override
    @JsonProperty
    public String getCreatedBy() {
        return super.getCreatedBy();
    }

    @Override
    @JsonProperty
    public Instant getLastModifiedDate() {
        return super.getLastModifiedDate();
    }

    @Override
    @JsonProperty
    public String getLastModifiedBy() {
        return super.getLastModifiedBy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Customer customer = (Customer) o;
        if (customer.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), customer.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Customer{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", number='" + number + '\'' +
            ", status=" + status +
            ", sessionTimeoutS=" + sessionTimeoutS +
            ", passwordExpiration=" + passwordExpiration +
            ", market=" + market +
            ", contractStartDate=" + contractStartDate +
            ", contractEndDate=" + contractEndDate +
            ", bdOwner='" + bdOwner + '\'' +
            ", analysts='" + analysts + '\'' +
            ", notes='" + notes + '\'' +
            ", deleted='" + deleted + '\'' +
            ", createdBy=" + getCreatedBy() +
            ", createdDate=" + getCreatedDate() +
            ", lastModifiedBy='" + getLastModifiedBy() + '\'' +
            ", lastModifiedDate=" + getLastModifiedDate() +
            '}';
    }
}
