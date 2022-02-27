package com.trss.bi.service.mapper;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.UserDetail;
import com.trss.bi.service.dto.CustomerDTO;
import com.trss.bi.service.dto.UserDetailDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Customer and its DTO CustomerDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface UserDetailMapper extends EntityMapper<UserDetailDTO, UserDetail> {



    default UserDetail fromId(Long id) {
        if (id == null) {
            return null;
        }
        UserDetail userDetail = new UserDetail();
        userDetail.setId(id);
        return userDetail;
    }
}
