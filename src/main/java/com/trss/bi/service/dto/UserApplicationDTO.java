package com.trss.bi.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the UserApplication entity.
 */
public class UserApplicationDTO implements Serializable {

    private Long id;

    @NotNull
    private Boolean hide;

    private Long userId;

    private String userLogin;

    private Long applicationId;

    private String applicationName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long jhi_userId) {
        this.userId = jhi_userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String jhi_userLogin) {
        this.userLogin = jhi_userLogin;
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

        UserApplicationDTO userApplicationDTO = (UserApplicationDTO) o;
        if (userApplicationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userApplicationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserApplicationDTO{" +
            "id=" + getId() +
            ", hide='" + isHide() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            ", application=" + getApplicationId() +
            ", application='" + getApplicationName() + "'" +
            "}";
    }
}
