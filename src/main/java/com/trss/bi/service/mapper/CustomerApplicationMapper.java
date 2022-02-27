package com.trss.bi.service.mapper;

import com.trss.bi.domain.*;
import com.trss.bi.service.dto.CustomerApplicationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CustomerApplication and its DTO CustomerApplicationDTO.
 */
@Mapper(componentModel = "spring", uses = {CustomerMapper.class, ApplicationMapper.class})
public interface CustomerApplicationMapper extends EntityMapper<CustomerApplicationDTO, CustomerApplication> {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "application.id", target = "applicationId")
    @Mapping(source = "application.name", target = "applicationName")
    CustomerApplicationDTO toDto(CustomerApplication customerApplication);

    @Mapping(source = "customerId", target = "customer")
    @Mapping(source = "applicationId", target = "application")
    CustomerApplication toEntity(CustomerApplicationDTO customerApplicationDTO);

    default CustomerApplication fromId(Long id) {
        if (id == null) {
            return null;
        }
        CustomerApplication customerApplication = new CustomerApplication();
        customerApplication.setId(id);
        return customerApplication;
    }
}
