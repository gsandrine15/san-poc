package com.trss.bi.service;

import com.trss.bi.domain.UserApplication;
import com.trss.bi.domain.UserDetail;
import com.trss.bi.repository.UserApplicationRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import com.trss.bi.web.rest.errors.CustomerAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

/**
 * Service Implementation for managing UserApplication.
 */
@Service
@Transactional
public class UserApplicationService {

    private final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    private static final String CACHE_USER_APPLICATIONS = "userApplications";
    private static final String ENTITY_NAME = "userApplication";

    private final UserApplicationRepository userApplicationRepository;

    private final UserDetailService userDetailService;

    private final CacheManager cacheManager;

    public UserApplicationService(UserApplicationRepository userApplicationRepository, UserDetailService userDetailService, CacheManager cacheManager) {
        this.userApplicationRepository = userApplicationRepository;
        this.userDetailService = userDetailService;
        this.cacheManager = cacheManager;
    }

    /**
     * Get userApplications by User
     *
     * @param user_id to get applications for
     * @return the list of User Applications
     */
    @Transactional(readOnly = true)
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public List<UserApplication> findAllByUserId(Long user_id) {
        log.debug("Request to get all UserApplications");

        // Check user is valid
        Optional<UserDetail> maybeUserDetail = userDetailService.findOne(user_id);
        if (!maybeUserDetail.isPresent()) {
            throw new BadRequestAlertException("Invalid user", ENTITY_NAME, "invalidUser");
        }
        UserDetail userDetail = maybeUserDetail.get();

        // Check can edit users for this customer
        if (!SecurityUtils.canAdminCustomer(userDetail.getCustomer().getId())) {
            throw new CustomerAccessException();
        }

        return userApplicationRepository.findAllByUserId(user_id);
    }

    /**
     * Set userApplications for User
     *
     * @param user_id to set applications for
     * @param userApplications list of User Applications
     * @return the saved list of User Applications
     */
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public List<UserApplication> setApplicationsForUser(Long user_id, List<UserApplication> userApplications) {
        log.debug("Request to setApplicationsForUser");

        // Check user is valid
        Optional<UserDetail> maybeUserDetail = userDetailService.findOne(user_id);
        if (!maybeUserDetail.isPresent()) {
            throw new BadRequestAlertException("Invalid user", ENTITY_NAME, "invalidUser");
        }
        UserDetail userDetail = maybeUserDetail.get();

        // Check can edit users for this customer
        if (!SecurityUtils.canAdminCustomer(userDetail.getCustomer().getId())) {
            throw new CustomerAccessException();
        }

        // Check application list is valid
        if (userApplications.stream().anyMatch(a -> a.getUser().getId() == null || a.getApplication().getId() == null || a.isHide() == null)) {
            throw new BadRequestAlertException("Invalid list of user applications", ENTITY_NAME, "invalidList");
        }
        if (userApplications.stream().anyMatch(a -> !a.getUser().getId().equals(user_id))) {
            throw new BadRequestAlertException("Invalid list of user applications", ENTITY_NAME, "invalidList");
        }

        List<UserApplication> results = new ArrayList<>();
        userApplicationRepository.deleteAllByUserId(user_id);
        for (UserApplication userApplication : userApplications) {
            results.add(userApplicationRepository.save(userApplication));
        }

        // clear the user-applications cache after updating
        Objects.requireNonNull(cacheManager.getCache(CACHE_USER_APPLICATIONS)).clear();

        return results;
    }
}
