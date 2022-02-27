package com.trss.bi.service;

import com.trss.bi.domain.UserDetail;
import com.trss.bi.repository.UserDetailRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import com.trss.bi.web.rest.errors.CustomerAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.*;

/**
 * Service Implementation for managing Customer.
 */
@Service
@Transactional
public class UserDetailService {

    private final Logger log = LoggerFactory.getLogger(UserDetailService.class);

    private final UserDetailRepository userDetailRepository;

    private static final String ENTITY_NAME = "userDetail";

    public UserDetailService(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    /**
     * Save user detail
     * Save method requires id to be set on insert
     *
     * @param userDetail the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public UserDetail save(UserDetail userDetail) {
        log.debug("Request to save UserDetail : {}", userDetail);

        if (userDetail.getCustomer() == null || userDetail.getCustomer().getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        // Check user can save this record
        if (!SecurityUtils.canAdminCustomer(userDetail.getCustomer().getId()) && !SecurityUtils.getCurrentUserId().equals(userDetail.getId())) {
            throw new CustomerAccessException();
        }

        // Check customer isn't changing
        Optional<UserDetail> maybeUserDetail = findOne(userDetail.getId());
        if (maybeUserDetail.isPresent()) {
            Long newCustomerId = maybeUserDetail.get().getCustomer().getId();
            if (!userDetail.getCustomer().getId().equals(newCustomerId)) {
                throw new CustomerAccessException();
            }
        }

        return userDetailRepository.save(userDetail);
    }


    /**
     * Get one user detail by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<UserDetail> findOne(Long id) {
        log.debug("Request to get UserDetail : {}", id);

        // Check user can view this record
        Optional<UserDetail> maybeUserDetail = userDetailRepository.findById(id);
        if (maybeUserDetail.isPresent()) {
            UserDetail userDetail = maybeUserDetail.get();
            // Must be appropriate Admin / Customer Admin or the User Themselves
            if (!SecurityUtils.canAdminCustomer(userDetail.getCustomer().getId()) && !SecurityUtils.getCurrentUserId().equals(id)) {
                throw new CustomerAccessException();
            }
        }

        return maybeUserDetail;
    }

    public Optional<UserDetail> findOneNoAuth(Long id) {
        log.debug("Request to get UserDetail noAuth : {}", id);
        return userDetailRepository.findById(id);
    }

    /**
     * Delete the user detail by id.
     *
     * @param id the id of the entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public void delete(Long id) {
        log.debug("Request to delete UserDetail : {}", id);

        // Check user can delete this record
        Optional<UserDetail> maybeUserDetail = userDetailRepository.findById(id);
        if (maybeUserDetail.isPresent()) {
            UserDetail userDetail = maybeUserDetail.get();
            if (!SecurityUtils.canAdminCustomer(userDetail.getCustomer().getId())) {
                throw new CustomerAccessException();
            }

            // Soft delete
            userDetail.setDeleted(true);
            this.save(userDetail);
        }
    }
}
