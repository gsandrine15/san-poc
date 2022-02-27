package com.trss.bi.repository;

import com.trss.bi.domain.Application;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the Application entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    String ALL_APPLICATIONS = "allApplications";

    @Cacheable(cacheNames = ALL_APPLICATIONS)
    List<Application> findAllBySystemAssignedIsFalse();
}
