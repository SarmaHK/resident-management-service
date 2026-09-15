package com.ams.resident.repository;

import com.ams.resident.entity.UserStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStatusHistoryRepository extends JpaRepository<UserStatusHistory, String> {
    List<UserStatusHistory> findByUserIdOrderByChangedAtDesc(String userId);
}
