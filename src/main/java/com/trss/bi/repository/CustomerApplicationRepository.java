package com.trss.bi.repository;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.CustomerApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the CustomerApplication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomerApplicationRepository extends JpaRepository<CustomerApplication, Long> {
    List<CustomerApplication> findAllByCustomer(Customer customer);
    void deleteAllByCustomer(Customer customer);
}
