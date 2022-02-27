package com.trss.bi.repository;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByCustomerAndName(Customer customer, String name);

    Optional<Role> findOneByCustomerAndId(Customer customer, Long id);

    List<Role> findAllByCustomerId(Long customerId);

    List<Role> findAllByCustomerIdAndNameIsNot(Long customerId, String name);
}
