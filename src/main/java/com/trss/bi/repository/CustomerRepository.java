package com.trss.bi.repository;

import com.trss.bi.domain.Customer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Customer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    String ALL_CUSTOMERS = "allCustomers";

    // disable caching the customers for now
    //@Cacheable(cacheNames = ALL_CUSTOMERS)
    Page<Customer> findAllBy(Pageable pageable);

    Page<Customer> findAllByDeletedFalse(Pageable pageable);

    Page<Customer> findAllByNameAndDeletedFalse(Pageable pageable, String name);
}
