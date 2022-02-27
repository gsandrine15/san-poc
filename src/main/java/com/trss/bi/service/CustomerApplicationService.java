package com.trss.bi.service;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.CustomerApplication;
import com.trss.bi.repository.CustomerApplicationRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import com.trss.bi.web.rest.errors.CustomerAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Access;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_ADMIN;
import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

/**
 * Service Implementation for managing CustomerApplication.
 */
@Service
@Transactional
public class CustomerApplicationService {

    private final Logger log = LoggerFactory.getLogger(CustomerApplicationService.class);

    private final CustomerApplicationRepository customerApplicationRepository;

    private final CustomerService customerService;

    private final CacheManager cacheManager;

    private static final String USER_APPLICATIONS = "userApplications";
    private static final String ENTITY_NAME = "customerApplication";

    public CustomerApplicationService(CustomerApplicationRepository customerApplicationRepository, CustomerService customerService, CacheManager cacheManager) {
        this.customerApplicationRepository = customerApplicationRepository;
        this.customerService = customerService;
        this.cacheManager = cacheManager;
    }

    /**
     * Get customerApplications by Customer
     *
     * @param customer_id to get applications for
     * @return the list of Customer Applications
     */
    @Transactional(readOnly = true)
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public List<CustomerApplication> findAllByCustomerId(Long customerId) {
        log.debug("Request to get all CustomerApplications");

        // Check customer is valid
        Optional<Customer> maybeCustomer = customerService.findOne(customerId);
        if (!maybeCustomer.isPresent()) {
            throw new BadRequestAlertException("Invalid customer", ENTITY_NAME, "invalidCustomer");
        }
        Customer customer = maybeCustomer.get();

        if (!SecurityUtils.canAdminCustomer(customer.getId())) {
            throw new CustomerAccessException();
        }
        return customerApplicationRepository.findAllByCustomer(customer);
    }

    /**
     * Set customerApplications for customer
     *
     * @param customer_id to save applications for
     * @param customerApplications list of Customer Applications
     * @return the saved list of Customer Applications
     */
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public List<CustomerApplication> setApplicationsForCustomerId(Long customerId, List<CustomerApplication> customerApplications) {
        log.debug("Request to setApplicationsForCustomer");

        // Check customer is valid
        Optional<Customer> maybeCustomer = customerService.findOne(customerId);
        if (!maybeCustomer.isPresent()) {
            throw new BadRequestAlertException("Invalid customer", ENTITY_NAME, "invalidCustomer");
        }
        Customer customer = maybeCustomer.get();

        // If not admin and customers don't match
        if (!SecurityUtils.canAdminCustomer(customer.getId())){
            throw new CustomerAccessException();
        }

        // Check application list is valid
        if (customerApplications.stream().anyMatch(a -> a.getCustomer().getId() == null || a.getApplication().getId() == null || a.getStatus() == null)) {
            throw new BadRequestAlertException("Invalid list of customer applications", ENTITY_NAME, "invalidList");
        }
        if (customerApplications.stream().anyMatch(a -> a.getCustomer().getId() != customer.getId())) {
            throw new BadRequestAlertException("Invalid list of customer applications", ENTITY_NAME, "invalidList");
        }

        List<CustomerApplication> results = new ArrayList<>();
        customerApplicationRepository.deleteAllByCustomer(customer);
        for (CustomerApplication customerApplication : customerApplications) {
            results.add(customerApplicationRepository.save(customerApplication));
        }

        // the user application cache is dependent on the customer applications, so clear that
        Objects.requireNonNull(cacheManager.getCache(USER_APPLICATIONS)).clear();

        return results;
    }
}
