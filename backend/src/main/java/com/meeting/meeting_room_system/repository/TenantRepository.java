package com.meeting.meeting_room_system.repository;

import com.meeting.meeting_room_system.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Tenant findByCode(String code);
}
