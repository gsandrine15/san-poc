package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.trss.bi.domain.Customer;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.service.CustomerService;
import com.trss.bi.web.rest.util.HeaderUtil;
import com.trss.bi.web.rest.util.PaginationUtil;
import com.trss.bi.web.rest.util.RequestParamUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.*;

/**
 * REST controller for managing Customer.
 */
@RestController
@RequestMapping("/api")
public class CustomerResource {

    private final Logger log = LoggerFactory.getLogger(CustomerResource.class);

    private static final String ENTITY_NAME = "customer";

    private final CustomerService customerService;

    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * POST  /customers : Create a new customer.
     *
     * @param customer the customer to create
     * @return the ResponseEntity with status 201 (Created) and with body the new customer, or with status 400 (Bad Request) if the customer has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/customers")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) throws URISyntaxException {
        log.debug("REST request to save Customer : {}", customer);

        Customer result = customerService.insert(customer);

        return ResponseEntity.created(new URI("/api/customers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /customers : Updates an existing customer.
     *
     * @param customer the customer to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated customer,
     * or with status 400 (Bad Request) if the customer is not valid,
     * or with status 500 (Internal Server Error) if the customer couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/customers")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<Customer> updateCustomer(@Valid @RequestBody Customer customer) {
        log.debug("REST request to update Customer : {}", customer);

        Customer result = customerService.update(customer);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, customer.getId().toString()))
            .body(result);
    }

    /**
     * GET  /customers : get all the customers.
     *
     * @param pageable the pagination information
     *
     * @return the ResponseEntity with status 200 (OK) and the list of customers in body
     */
    @GetMapping("/customers")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public ResponseEntity<List<Customer>> getAllCustomers(Pageable pageable, @RequestParam Map<String, String> params) {
        log.debug("REST request to get a page of Customers, {}", params);

        Map<String, String> searchFacets = RequestParamUtil.removePageableParams(params);

        if (SecurityUtils.isCurrentUserInRole(CUSTOMER_ADMIN)) {
            Page<Customer> page = new PageImpl(Lists.newArrayList(customerService.findOne(SecurityUtils.getCurrentCustomerId()).get()));
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/customers");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }

        Page<Customer> page;
        if (searchFacets.isEmpty()) {
            page = customerService.findAll(pageable);
        } else {
            page = customerService.findAllByPagingCriteria(pageable, searchFacets);
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/customers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /customers/:id : get the "id" customer.
     *
     * @param id the id of the customer to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the customer, or with status 404 (Not Found)
     */
    @GetMapping("/customers/{id}")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        log.debug("REST request to get Customer : {}", id);
        Optional<Customer> customer = customerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(customer);
    }

    /**
     * DELETE  /customers/:id : delete the "id" customer.
     *
     * @param id the id of the customer to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/customers/{id}")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.debug("REST request to delete Customer : {}", id);
        customerService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
