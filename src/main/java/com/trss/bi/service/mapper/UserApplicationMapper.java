package com.trss.bi.service.mapper;

import com.trss.bi.domain.*;
import com.trss.bi.service.dto.UserApplicationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity UserApplication and its DTO UserApplicationDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ApplicationMapper.class})
public interface UserApplicationMapper extends EntityMapper<UserApplicationDTO, UserApplication> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    @Mapping(source = "application.id", target = "applicationId")
    @Mapping(source = "application.name", target = "applicationName")
    UserApplicationDTO toDto(UserApplication userApplication);

    @Mapping(source = "userId", target = "user")
    @Mapping(source = "applicationId", target = "application")
    UserApplication toEntity(UserApplicationDTO userApplicationDTO);

    default UserApplication fromId(Long id) {
        if (id == null) {
            return null;
        }
        UserApplication userApplication = new UserApplication();
        userApplication.setId(id);
        return userApplication;
    }
}
