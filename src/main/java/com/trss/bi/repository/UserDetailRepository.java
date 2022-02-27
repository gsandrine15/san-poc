package com.trss.bi.repository;

import com.trss.bi.domain.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Customer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

}
