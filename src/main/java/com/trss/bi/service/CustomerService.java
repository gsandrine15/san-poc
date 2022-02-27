package com.trss.bi.service;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.CustomerStatus;
import com.trss.bi.repository.CustomerRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.service.dto.RoleDTO;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_ADMIN;
import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

/**
 * Service Implementation for managing Customer.
 */
@Service
@Transactional
public class CustomerService {

    private final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final RoleService roleService;
    private final CustomerRepository customerRepository;

    private final CacheManager cacheManager;

    private static final String CUSTOMER = "customer";

    public CustomerService(CustomerRepository customerRepository, CacheManager cacheManager, RoleService roleService) {
        this.customerRepository = customerRepository;
        this.cacheManager = cacheManager;
        this.roleService = roleService;
    }

    /**
     * Insert a new customer
     *
     * @param customer the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Customer insert(Customer customer) {
        if (customer.getId() != null) {
            throw new BadRequestAlertException("A new customer cannot already have an ID", CUSTOMER, "idexists");
        }
        if (customerRepository.findAllByNameAndDeletedFalse(null, customer.getName()).stream().count() > 0L) {
            throw new IllegalStateException("Customer name: \"" + customer.getName() + "\" already exists!");
        }
        Customer savedCustomer = save(customer);

        // Create default roles
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setCustomer(customer);
        roleDTO.setName(AuthorizationConstants.USER);
        roleService.createSystemRole(roleDTO);
        roleDTO.setName(AuthorizationConstants.CUSTOMER_ADMIN);
        roleService.createSystemRole(roleDTO);
        return savedCustomer;
    }

    /**
     * Update an existing customer
     *
     * @param customer the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Customer update(Customer customer) {
        if (customer.getId() == null) {
            throw new BadRequestAlertException("Invalid id", CUSTOMER, "idnull");
        }
        return save(customer);
    }

    /**
     * Save a customer.
     *
     * @param customer the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Customer save(Customer customer) {
        log.debug("Request to save Customer : {}", customer);
        clearCustomerCaches(customer);
        return customerRepository.save(customer);
    }

    /**
     * Get all the customers.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        log.debug("Request to get all Customers");
        return customerRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Get all the customers by customer name. This will return a list of 1 or 0.
     * @param pageable the pagination information
     * @return the list of entities
     */
    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public Page<Customer> findAllByName(Pageable pageable, String name) {
        log.debug("Request to get customers by name, '{}'", name);
        return customerRepository.findAllByNameAndDeletedFalse(pageable, name);
    }

    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public Page<Customer> findAllByPagingCriteria(Pageable pageable, Map<String, String> facets) {
        log.debug("Request to get customers by paging criteria, '{}'", facets);
        Page<Customer> page = customerRepository.findAll(new Specification<Customer>() {
            @Override
            public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                // Never return deleted customers
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("deleted"), Boolean.valueOf(false))));

                if (facets.containsKey("name")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("name"), "%" + facets.get("name") + "%")));
                }

                if (facets.containsKey("number")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("number"), "%" + facets.get("number") + "%")));
                }

                if (facets.containsKey("status")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), CustomerStatus.valueOf(facets.get("status")))));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);

        return page;
    }

    /**
     * Get one customer by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    @Transactional(readOnly = true)
    public Optional<Customer> findOne(Long id) {
        log.debug("Request to get Customer : {}", id);
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Integer findSessionTimeoutNoAuth(Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            return customer.get().getSessionTimeoutS();
        }

        return null;
     }

    /**
     * Delete the customer by id.
     *
     * @param id the id of the entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public void delete(Long id) {
        log.debug("Request to delete Customer : {}", id);
        Optional<Customer> maybeCustomer = this.findOne(id);
        if ( maybeCustomer.isPresent() ) {
           Customer customer = maybeCustomer.get();
           customer.setDeleted(true);
           this.save(customer);
        }
        clearAllCustomerCache();
    }

    /**
     * Clear customer caches for the entity specified
     *
     * @param customer the customer that was updated
     */
    private void clearCustomerCaches(Customer customer) {
        clearAllCustomerCache();
    }

    /**
     * Clear the all customer cache
     * Note: This should happen after any CRUD operation for a customer
     */
    private void clearAllCustomerCache() {
        Objects.requireNonNull(cacheManager.getCache(CustomerRepository.ALL_CUSTOMERS)).clear();
    }
}
