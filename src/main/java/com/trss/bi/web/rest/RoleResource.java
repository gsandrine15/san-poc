package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.trss.bi.service.RoleService;
import com.trss.bi.service.dto.RoleDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

@RestController
@RequestMapping("/api")
public class RoleResource {

    private final RoleService roleService;

    public RoleResource(RoleService roleService) {
        this.roleService = roleService;
    }
    /**
     * @return a list of the all of the roles
     */
    @GetMapping("/roles")
    @Timed
    public List<RoleDTO> getRoles() {
        return roleService.getRoles();
    }

    /**
     * Creates a role with authorities
     * @param roleDTO
     * @return the created role
     */
    @PostMapping("/roles/role")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public RoleDTO createRole(@RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    /**
     * Updates a role with authorities
     * @param roleDTO
     * @return the created role
     */
    @PutMapping("/roles/role")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public RoleDTO updateRole(@RequestBody RoleDTO roleDTO) {
        return roleService.updateRole(roleDTO);
    }

    /**
     *
     */
    @DeleteMapping("roles/role/{id}")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }
}
