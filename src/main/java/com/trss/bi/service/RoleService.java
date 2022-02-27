package com.trss.bi.service;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.Role;
import com.trss.bi.repository.CustomerRepository;
import com.trss.bi.repository.RoleRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.service.dto.RoleDTO;
import com.trss.bi.service.mapper.RoleMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    private final RoleMapper roleMapper = new RoleMapper();
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final UserWithDetailService userWithDetailService;
    public RoleService(RoleRepository roleRepository,
                       CustomerRepository customerRepository,
                       UserWithDetailService userWithDetailService) {
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.userWithDetailService = userWithDetailService;
    }

    /**
     * @return a list of all the roles
     */
    public List<RoleDTO> getRoles() {
        return (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.CUSTOMER_ADMIN) ?
            roleRepository.findAllByCustomerIdAndNameIsNot(SecurityUtils.getCurrentCustomerId(), AuthorizationConstants.ADMIN) :
            roleRepository.findAll()).stream().map(role -> {
                Long userCount = userWithDetailService.countUsersByRoleId(role.getId());
                RoleDTO roleDTO = roleMapper.toDto(role);
                roleDTO.setUserCount(userCount);
                return roleDTO;
        }).collect(Collectors.toList());
    }

    private Role validateRole(RoleDTO roleMapping) {
        if (roleMapping.getId() != null && roleRepository.findById(roleMapping.getId()).isPresent()) {
            throw new IllegalStateException("Role " + roleMapping.getName() + " already exists");
        }
        if (roleMapping.getName().startsWith("ROLE_")) {
            throw new IllegalArgumentException("Cannot create a role with a \"ROLE_\" prefix");
        }
        if (roleRepository.findByCustomerAndName(roleMapping.getCustomer(), roleMapping.getName()) != null) {
            throw new IllegalStateException("Role " + roleMapping.getName() + " already exists for " + roleMapping.getCustomer().getName());
        }

        if (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.CUSTOMER_ADMIN)) {
            // must set customer to current customer for customer admins
            Customer customer = customerRepository.findById(SecurityUtils.getCurrentCustomerId()).orElseThrow(() -> new IllegalStateException("Unable to find customer"));
            roleMapping.setCustomer(customer);
        }

        Role role = new Role(roleMapping.getName());
        role.setCustomer(roleMapping.getCustomer());
        role.setAuthorities(roleMapping.getAuthorities());
        return role;
    }

    public RoleDTO createSystemRole(RoleDTO roleMapping) {
        Role role = validateRole(roleMapping);

        // Temporarily clear SecurityContext (To save as system)
        SecurityContext ctx = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
        Role savedRole = roleRepository.save(role);

        // Restore SecurityContext
        SecurityContextHolder.setContext(ctx);
        return roleMapper.toDto(savedRole);
    }

    public RoleDTO createRole(RoleDTO roleMapping) {
        Role role = validateRole(roleMapping);
        if (AuthorizationConstants.RESERVED_ROLES.contains(roleMapping.getName())) {
            throw new IllegalStateException("Role " + roleMapping.getName() + " is a reserved role");
        }
        return roleMapper.toDto(roleRepository.save(role));
    }

    public RoleDTO updateRole(RoleDTO roleMapping) {
        if (AuthorizationConstants.ADMIN.equals(roleMapping.getName()) ||
            AuthorizationConstants.CUSTOMER_ADMIN.equals(roleMapping.getName()) ||
            AuthorizationConstants.USER.equals(roleMapping.getName())
        ) {
            throw new IllegalArgumentException(roleMapping.getName() + " cannot be modified");
        }
        if (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.CUSTOMER_ADMIN)) {
            // must set customer to current customer for customer admins
            Customer customer = customerRepository.findById(SecurityUtils.getCurrentCustomerId()).orElseThrow(() -> new IllegalStateException("Unable to find customer"));
            roleMapping.setCustomer(customer);
        }
        Role role = null;
        if (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.ADMIN)) {
            role = roleRepository.findById(roleMapping.getId())
                .orElseThrow(() -> new IllegalArgumentException("RoleId " + roleMapping.getId() + " does not exist"));
        } else {
            // user making change is NOT ROLE_ADMIN (ie, ROLE_CUSTOMER_ADMIN)
            if (roleMapping.getAuthorities().stream().anyMatch(a -> "ADMIN_TOOLS".equals(a.getName()) || "ADMIN_CONSOLE".equals(a.getName()))) {
                throw new IllegalArgumentException("Customer admins cannot assign admin privileges");
            }
            Customer customer = customerRepository.findById(SecurityUtils.getCurrentCustomerId()).orElseThrow(() -> new IllegalStateException("Unable to find customer"));
            // get their role for this customer
            role = roleRepository.findOneByCustomerAndId(customer, roleMapping.getId())
                .orElseThrow(() -> new IllegalArgumentException("RoleId " + roleMapping.getId() + " for customer " + customer.getName() + " does not exist"));
        }
        role.setAuthorities(roleMapping.getAuthorities());
        return roleMapper.toDto(roleRepository.save(role));
    }

    public void deleteRole(Long roleId) {
        if (userWithDetailService.roleInUse(roleId)) {
            throw new IllegalArgumentException("Role must not be in use");
        }
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("roleId " + roleId + " not found!"));
        if (AuthorizationConstants.ADMIN.equals(role.getName()) ||
            AuthorizationConstants.CUSTOMER_ADMIN.equals(role.getName()) ||
            AuthorizationConstants.USER.equals(role.getName())) {
            throw new IllegalArgumentException(role.getName() + " cannot be deleted");
        }
        if (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.CUSTOMER_ADMIN)) {
            Customer customer = customerRepository.findById(SecurityUtils.getCurrentCustomerId()).orElseThrow(() -> new IllegalStateException("Unable to find customer"));
            if (!role.getCustomer().equals(customer)) {
                throw new IllegalArgumentException("Customer admins cannot delete roles with a different customer");
            }
        }
        roleRepository.deleteById(roleId);
    }

}
