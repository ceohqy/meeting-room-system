package com.meeting.meeting_room_system.repository;

import com.meeting.meeting_room_system.entity.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord,Long> {
    Optional<ApprovalRecord> findByReservationId(Long reservationId);
    long countByApprovalResult(String approvalResult);

    // 统计待审批 - 全部
    // 说明：
    // 1) 历史数据可能是 approval_result IS NULL（旧口径），新数据是 approval_result = 'PENDING'（新口径），两者都应视为待审批。
    // 2) 为避免“审批记录残留但 reservation 已被删除”的孤儿数据影响统计，这里要求 reservation 必须存在。
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE (a.approvalResult IS NULL OR a.approvalResult = 'PENDING') AND EXISTS (SELECT 1 FROM Reservation r WHERE r.id = a.reservationId)")
    long countPending();

    // 统计待审批 - 按租户
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE (a.approvalResult IS NULL OR a.approvalResult = 'PENDING') AND a.reservationId IN (SELECT r.id FROM Reservation r WHERE r.tenantId = ?1)")
    long countPendingByTenantId(Long tenantId);

    // 统计已通过（approval_result == 'APPROVED'）- 全部（要求 reservation 存在）
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE a.approvalResult = 'APPROVED' AND EXISTS (SELECT 1 FROM Reservation r WHERE r.id = a.reservationId)")
    long countApproved();

    // 统计已拒绝（approval_result == 'REJECTED'）- 全部（要求 reservation 存在）
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE a.approvalResult = 'REJECTED' AND EXISTS (SELECT 1 FROM Reservation r WHERE r.id = a.reservationId)")
    long countRejected();

    // 统计已通过（approval_result == 'APPROVED'）- 按租户
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE a.approvalResult = 'APPROVED' AND a.reservationId IN (SELECT r.id FROM Reservation r WHERE r.tenantId = ?1)")
    long countApprovedByTenantId(Long tenantId);

    // 统计已拒绝（approval_result == 'REJECTED'）- 按租户
    @Query("SELECT COUNT(a) FROM ApprovalRecord a WHERE a.approvalResult = 'REJECTED' AND a.reservationId IN (SELECT r.id FROM Reservation r WHERE r.tenantId = ?1)")
    long countRejectedByTenantId(Long tenantId);
}