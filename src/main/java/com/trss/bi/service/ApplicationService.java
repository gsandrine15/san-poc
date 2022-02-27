package com.trss.bi.service;

import com.trss.bi.domain.Application;
import com.trss.bi.repository.ApplicationRepository;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_ADMIN;

/**
 * Service Implementation for managing Application.
 */
@Service
@Transactional
public class ApplicationService {

    private final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;

    private static final String APPLICATION = "application";

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Insert a new customer
     *
     * @param application the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Application insert(Application application) {
        if (application.getId() != null) {
            throw new BadRequestAlertException("A new customer cannot already have an ID", APPLICATION, "idexists");
        }
        return save(application);
    }

    /**
     * Update an existing application
     *
     * @param application the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Application update(Application application) {
        if (application.getId() == null) {
            throw new BadRequestAlertException("Invalid id", APPLICATION, "idnull");
        }
        return save(application);
    }

    /**
     * Save a application.
     *
     * @param application the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public Application save(Application application) {
        log.debug("Request to save Application : {}", application);        return applicationRepository.save(application);
    }

    /**
     * Get all the applications.
     *
     * @return the list of entities
     */
    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public List<Application> findAll() {
        log.debug("Request to get all Applications");
        return applicationRepository.findAll();
    }

    /**
     * Get all the applications that are not system assigned
     *
     * @return the list of entities
     */
    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public List<Application> findAllNotSystemAssigned() {
        log.debug("Request to get all Applications not system defined");
        return applicationRepository.findAllBySystemAssignedIsFalse();
    }


    /**
     * Get one application by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    @Transactional(readOnly = true)
    public Optional<Application> findOne(Long id) {
        log.debug("Request to get Application : {}", id);
        return applicationRepository.findById(id);
    }

    /**
     * Get one application by id and skip authorization
     * Only use this during the login process
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<Application> findOneNoAuth(Long id) {
        log.debug("Request to get Application : {}", id);
        return applicationRepository.findById(id);
    }

    /**
     * Delete the application by id.
     *
     * @param id the id of the entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public void delete(Long id) {
        log.debug("Request to delete Application : {}", id);
        applicationRepository.deleteById(id);
    }
}
