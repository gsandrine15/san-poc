package com.trss.bi.repository;

import com.trss.bi.domain.UserApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the UserApplication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {

    @Query("select user_application from UserApplication user_application where user_application.user.login = ?#{principal.username}")
    List<UserApplication> findByUserIsCurrentUser();

    List<UserApplication> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

}
