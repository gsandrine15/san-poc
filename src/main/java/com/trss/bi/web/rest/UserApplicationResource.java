package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.trss.bi.domain.UserApplication;
import com.trss.bi.service.UserApplicationService;
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
 * REST controller for managing UserApplication.
 */
@RestController
@RequestMapping("/api")
public class UserApplicationResource {

    private final Logger log = LoggerFactory.getLogger(UserApplicationResource.class);

    private final UserApplicationService userApplicationService;

    public UserApplicationResource(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * GET  /user-applications/:id : get userApplications settings for the specified user
     *
     * @param id the id of the user to retrieve userApplications settings for
     * @return the ResponseEntity with status 200 (OK) and with body array of UserApplications, or with status 404 (Not Found)
     */
    @GetMapping("/user-applications/{id}")
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    @Timed
    public List<UserApplication> getUserApplications(@PathVariable Long id) {
        log.debug("REST request to get UserApplication : {}", id);
        return userApplicationService.findAllByUserId(id);
    }

    /**
     * PUT  /user-applications : Updates userApplications settings for the specified user.
     *
     * @param userApplications the userApplications to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated array of userApplications,
     * or with status 400 (Bad Request) if the userApplications is not valid,
     * or with status 500 (Internal Server Error) if the userApplications couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/user-applications/{id}")
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    @Timed
    public List<UserApplication> updateUserApplication(@PathVariable Long id, @Valid @RequestBody List<UserApplication> userApplications) {
        log.debug("REST request to set UserApplication : {}", id);
        return userApplicationService.setApplicationsForUser(id, userApplications);
    }
}
