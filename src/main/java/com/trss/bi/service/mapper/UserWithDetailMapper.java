package com.trss.bi.service.mapper;

import com.trss.bi.domain.Authority;
import com.trss.bi.domain.Role;
import com.trss.bi.domain.User;
import com.trss.bi.domain.UserWithDetail;
import com.trss.bi.service.dto.UserWithDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserWithDetailMapper {

    public UserWithDetailDTO userToUserDTO(UserWithDetail user) {
        return new UserWithDetailDTO(user);
    }

    public List<UserWithDetailDTO> usersToUserDTOs(List<UserWithDetail> users) {
        return users.stream()
            .filter(Objects::nonNull)
            .map(this::userToUserDTO)
            .collect(Collectors.toList());
    }

    public UserWithDetail userDTOToUser(UserWithDetailDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            UserWithDetail user = new UserWithDetail();
            user.setId(userDTO.getId());
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setImageUrl(userDTO.getImageUrl());
            user.setActivated(userDTO.isActivated());
            user.setLangKey(userDTO.getLangKey());
            user.setRole(userDTO.getRole());
            user.setCustomer(userDTO.getCustomer());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            return user;
        }
    }

    public List<UserWithDetail> userDTOsToUsers(List<UserWithDetailDTO> userDTOs) {
        return userDTOs.stream()
            .filter(Objects::nonNull)
            .map(this::userDTOToUser)
            .collect(Collectors.toList());
    }

    public User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    public Set<Authority> authoritiesFromStrings(Set<String> strings) {
        return strings.stream().map(string -> {
            Authority auth = new Authority();
            auth.setName(string);
            return auth;
        }).collect(Collectors.toSet());
    }
}
