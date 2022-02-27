package com.trss.bi.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_application_access")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserApplicationAccess implements Serializable {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationAssignmentStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserWithDetail userWithDetail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public ApplicationAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationAssignmentStatus status) {
        this.status = status;
    }

    public UserWithDetail getUserWithDetail() {
        return userWithDetail;
    }

    public void setUserWithDetail(UserWithDetail userWithDetail) {
        this.userWithDetail = userWithDetail;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public  UserApplicationAccess() {
    }

    public UserApplicationAccess(UserWithDetail userWithDetail, Application application, ApplicationAssignmentStatus status) {
        setId(userWithDetail.getId() + "-" + application.getId());
        setUserWithDetail(userWithDetail);
        setApplication(application);
        setStatus(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserApplicationAccess userApplicationAccess = (UserApplicationAccess) o;
        if (userApplicationAccess.getUserWithDetail() == null || getUserWithDetail() == null || userApplicationAccess.getApplication() == null || getApplication() == null) {
            return false;
        }
        return Objects.equals(getUserWithDetail(), userApplicationAccess.getUserWithDetail()) && Objects.equals(getApplication(), userApplicationAccess.getApplication());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUserWithDetail().toString() + getApplication().toString());
    }

    @Override
    public String toString() {
        return "CustomerApplication{" +
            " userdetail=" + getUserWithDetail() +
            ", application=" + getApplication() +
            ", status=" + getStatus() +
            "}";
    }
}

