package com.meeting.meeting_room_system.repository;

import com.meeting.meeting_room_system.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment,Long> {
    long countByTenantId(Long tenantId);
}