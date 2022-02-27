package com.trss.bi.service.mapper;

import com.trss.bi.domain.Authority;
import com.trss.bi.service.dto.AuthorityDTO;

public class AuthorityMapper extends BaseEntityMapper<AuthorityDTO, Authority> {
    @Override
    public Authority toEntity(AuthorityDTO dto) {
        if (dto == null) {
            return null;
        }
        Authority entity = new Authority();
        entity.setName(dto.getName());
        return null;
    }

    @Override
    public AuthorityDTO toDto(Authority entity) {
        if (entity == null) {
            return null;
        }
        AuthorityDTO dto = new AuthorityDTO();
        dto.setName(entity.getName());
        return dto;
    }
}
