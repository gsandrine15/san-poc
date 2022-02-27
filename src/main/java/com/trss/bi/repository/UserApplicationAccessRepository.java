package com.trss.bi.repository;

import com.trss.bi.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the CustomerApplication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserApplicationAccessRepository extends JpaRepository<UserApplicationAccess, String> {
    List<UserApplicationAccess> findAllByUserWithDetailId(Long id);
}
