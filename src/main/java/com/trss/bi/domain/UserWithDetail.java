package com.trss.bi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trss.bi.config.Constants;
import com.trss.bi.service.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A user with detail
 */
@Entity
@Table(name = "user_with_detail")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserWithDetail extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60)//, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 6)
    @Column(name = "lang_key", length = 6)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
        name = "jhi_user_role",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_date")
    private Instant passwordDate = Instant.now();

    @Column(nullable = false)
    private boolean deleted = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
        return resetDate;
    }

    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Set<Authority> getAuthorities() {
        return this.role.getAuthorities();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

        User user = (User) o;
        return !(user.getId() == null || getId() == null) && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "User{" +
            "login='" + this.getLogin() + '\'' +
            ", firstName='" + this.getFirstName() + '\'' +
            ", lastName='" + this.getLastName() + '\'' +
            ", email='" + this.getEmail() + '\'' +
            ", imageUrl='" + this.getImageUrl() + '\'' +
            ", activated='" + this.getActivated() + '\'' +
            ", langKey='" + this.getLangKey() + '\'' +
            ", activationKey='" + this.getActivationKey() + '\'' +
            ", customer='" + this.getCustomer() + "'" +
            ", phoneNumber='" + this.getPhoneNumber() + "'" +
            ", passwordDate'" + this.getPasswordDate() + "'" +
            ", deleted'" + this.getDeleted() + "'" +
            "}";
    }

    public UserDTO updateUserDTO(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            userDTO.setId(this.getId());
            userDTO.setLogin(this.getLogin());
            userDTO.setFirstName(this.getFirstName());
            userDTO.setLastName(this.getLastName());
            userDTO.setEmail(this.getEmail());
            userDTO.setImageUrl(this.getImageUrl());
            userDTO.setActivated(this.getActivated());
            userDTO.setLangKey(this.getLangKey());
            userDTO.setAuthorities(Stream.concat(this.getAuthorities().stream().map(Authority::getName), Stream.of(this.getRole().getAuthority())).collect(Collectors.toSet()));
            userDTO.setRole(this.getRole().getName());
            return userDTO;
        }
    }

    public UserDetail updateUserDetail(UserDetail userDetail) {
        if (userDetail == null) {
            return null;
        } else {
            userDetail.setId(this.getId());
            userDetail.setCustomer(this.getCustomer());
            userDetail.setPhoneNumber(this.getPhoneNumber());
            userDetail.setDeleted(this.deleted);
            return userDetail;
        }
    }
}
