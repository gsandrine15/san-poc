package com.trss.bi.service.mapper;

import com.trss.bi.domain.Role;
import com.trss.bi.service.dto.RoleDTO;

import java.util.stream.Collectors;

public class RoleMapper extends BaseEntityMapper<RoleDTO, Role> {

    private AuthorityMapper authorityMapper = new AuthorityMapper();

    @Override
    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        Role entity = new Role();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setAuthorities(dto.getAuthorities());
        entity.setCustomer(dto.getCustomer());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedDate(dto.getLastModifiedDate());
        return entity;
    }

    /**
     * Maps a role to a role DTO. <br/>
     * <strong>NOTE</strong> this doesn't set userCount though userCount is in roleDTO. If you wish to add userCount, please do so after using this method.
     * @param entity
     * @return
     */
    @Override
    public RoleDTO toDto(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAuthorities(entity.getAuthorities());
        dto.setCustomer(entity.getCustomer());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        return dto;
    }
}
