package com.trss.bi.repository;

import com.trss.bi.domain.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findAllByUserIdOrderByCreatedDateAsc(Long userId);
    void deleteAllByUserId(Long userId);
    List<PasswordHistory> findAllByUserId(Long userId);
}
