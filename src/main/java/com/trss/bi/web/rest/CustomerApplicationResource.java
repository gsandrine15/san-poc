package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.trss.bi.domain.CustomerApplication;
import com.trss.bi.service.CustomerApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_ADMIN;
import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

/**
 * REST controller for managing CustomerApplication.
 */
@RestController
@RequestMapping("/api")
public class CustomerApplicationResource {

    private final Logger log = LoggerFactory.getLogger(CustomerApplicationResource.class);

    private final CustomerApplicationService customerApplicationService;

    public CustomerApplicationResource(CustomerApplicationService customerApplicationService) {
        this.customerApplicationService = customerApplicationService;
    }

    /**
     * GET  /customer-applications/:id : get customerApplications settings for the customer
     *
     * @param id the id of the user to retrieve userApplications settings for
     * @return the ResponseEntity with status 200 (OK) and with body array of UserApplications, or with status 404 (Not Found)
     */
    @GetMapping("/customer-applications/{id}")
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    @Timed
    public List<CustomerApplication> getCustomerApplications(@PathVariable Long id) {
        log.debug("REST request to get CustomerApplication : {}", id);
        return customerApplicationService.findAllByCustomerId(id);
    }

    /**
     * PUT  /customer-applications : Updates an existing customerApplication.
     *
     * @param customerApplications the customerApplication to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated customerApplication,
     * or with status 400 (Bad Request) if the customerApplication is not valid,
     * or with status 500 (Internal Server Error) if the customerApplication couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/customer-applications/{id}")
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    @Timed
    public List<CustomerApplication> updateCustomerApplications(@PathVariable Long id, @Valid @RequestBody List<CustomerApplication> customerApplications) {
        log.debug("REST request to get updateCustomerApplications : {}", id);
        return customerApplicationService.setApplicationsForCustomerId(id, customerApplications);
    }
}
