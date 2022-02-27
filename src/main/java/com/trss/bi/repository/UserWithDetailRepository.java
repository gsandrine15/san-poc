package com.trss.bi.repository;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.UserWithDetail;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface UserWithDetailRepository extends JpaRepository<UserWithDetail, Long>, JpaSpecificationExecutor<UserWithDetail> {
    String USER_WITH_DETAIL_BY_ID = "userWithDetailById";
    String USER_WITH_DETAIL_BY_LOGIN = "userWithDetailByLogin";

    @EntityGraph(attributePaths = "role")
    Page<UserWithDetail> findAllWithRoleByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "role")
    Page<UserWithDetail> findAllWithRoleByCustomerAndDeletedFalse(Pageable pageable, Customer customer);

    boolean existsUserWithDetailByRoleId(Long roleId);

    Long countUsersByRoleIdAndDeletedFalse(Long roleId);

    Optional<UserWithDetail> findOneByLogin(String login);

    @EntityGraph(attributePaths = "role")
    @Cacheable(cacheNames = USER_WITH_DETAIL_BY_LOGIN)
    Optional<UserWithDetail> findOneWithRoleByLogin(String login);

    @EntityGraph(attributePaths = {"role", "authorities"})
    @Cacheable(cacheNames = USER_WITH_DETAIL_BY_ID)
    Optional<UserWithDetail> findOneWithRoleAndWithAuthoritiesById(Long id);
}
