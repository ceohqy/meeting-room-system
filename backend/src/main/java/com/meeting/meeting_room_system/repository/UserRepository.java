package com.meeting.meeting_room_system.repository;

import com.meeting.meeting_room_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    long countByTenantId(Long tenantId);
    List<User> findByTenantIdAndRole(Long tenantId, String role);
}