package com.meeting.meeting_room_system.service;

import com.meeting.meeting_room_system.dto.ApprovalListDTO;
import com.meeting.meeting_room_system.entity.ApprovalRecord;
import com.meeting.meeting_room_system.entity.Reservation;
import com.meeting.meeting_room_system.entity.Room;
import com.meeting.meeting_room_system.enums.ReservationStatus;
import com.meeting.meeting_room_system.exception.BusinessException;
import com.meeting.meeting_room_system.entity.Notification;
import com.meeting.meeting_room_system.repository.ApprovalRecordRepository;
import com.meeting.meeting_room_system.repository.NotificationRepository;
import com.meeting.meeting_room_system.repository.ReservationRepository;
import com.meeting.meeting_room_system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public List<ApprovalListDTO> listAll() {
        return listByTenantId(null);
    }

    public List<ApprovalListDTO> listByTenantId(Long tenantId) {
        List<ApprovalRecord> records = approvalRecordRepository.findAll();

        List<Long> reservationIds = records.stream().map(ApprovalRecord::getReservationId).collect(Collectors.toList());
        List<Reservation> reservations = reservationRepository.findAllById(reservationIds);

        // 租户过滤：非超级管理员只看本租户的预约对应的审批
        if (tenantId != null) {
            final Long tid = tenantId;
            reservations = reservations.stream()
                .filter(r -> tid.equals(r.getTenantId()))
                .collect(Collectors.toList());
        }

        List<Long> filteredReservationIds = reservations.stream().map(Reservation::getId).collect(Collectors.toList());
        List<Room> rooms = roomRepository.findAllById(
            reservations.stream().map(Reservation::getRoomId).collect(Collectors.toList())
        );

        final List<Reservation> finalReservations = reservations;
        return records.stream()
            .filter(record -> filteredReservationIds.contains(record.getReservationId()))
            .map(record -> {
                ApprovalListDTO dto = new ApprovalListDTO();
                dto.setId(record.getId());
                dto.setReservationId(record.getReservationId());

                finalReservations.stream().filter(r -> r.getId().equals(record.getReservationId())).findFirst().ifPresent(reservation -> {
                    rooms.stream().filter(room -> room.getId().equals(reservation.getRoomId())).findFirst().ifPresent(room ->
                        dto.setRoomName(room.getName())
                    );
                    dto.setApplicant(reservation.getApplicant());
                    dto.setStartTime(reservation.getReservationDate() + " " + reservation.getStartTime());
                    dto.setEndTime(reservation.getReservationDate() + " " + reservation.getEndTime());
                    dto.setAttendees(reservation.getAttendees());
                    dto.setPurpose(reservation.getPurpose());
                    dto.setApplyTime(reservation.getCreateTime() != null ?
                        reservation.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                    dto.setStatus(statusToInt(reservation.getStatus()));
                });

                return dto;
            }).filter(dto -> dto.getRoomName() != null).collect(Collectors.toList());
    }

    private int statusToInt(String status) {
        if (status == null) return 0;
        switch (status) {
            // 兼容历史种子数据：reservation.status 可能是 "0/1/2"
            case "PENDING":
            case "0":
                return 0;
            case "APPROVED":
            case "1":
                return 1;
            case "REJECTED":
            case "2":
                return 2;
            case "CANCELLED": return 3;
            default: return 0;
        }
    }

    @Transactional
    public ApprovalRecord approve(ApprovalRecord record) {
        ApprovalRecord existing = approvalRecordRepository.findByReservationId(record.getReservationId())
                .orElseThrow(() -> new BusinessException("审批记录不存在"));

        existing.setApprovalResult(ReservationStatus.APPROVED.name());
        existing.setApprovalTime(LocalDateTime.now());
        existing.setApproverId(record.getApproverId());
        existing.setRemark(record.getRemark());
        ApprovalRecord saved = approvalRecordRepository.save(existing);

        Reservation reservation = reservationRepository.findById(record.getReservationId())
                .orElseThrow(() -> new BusinessException("预约不存在"));
        reservation.setStatus(ReservationStatus.APPROVED.name());
        reservationRepository.save(reservation);

        // 发送通知给预约人
        Notification notification = new Notification();
        notification.setUserId(reservation.getUserId());
        notification.setTitle("预约审批通过");
        notification.setContent("您的预约「" + reservation.getPurpose() + "」已审批通过，请按时使用会议室。");
        notification.setType("APPROVED");
        notification.setReservationId(reservation.getId());
        notification.setTenantId(reservation.getTenantId());
        notificationRepository.save(notification);

        return saved;
    }

    @Transactional
    public ApprovalRecord reject(ApprovalRecord record) {
        ApprovalRecord existing = approvalRecordRepository.findByReservationId(record.getReservationId())
                .orElseThrow(() -> new BusinessException("审批记录不存在"));

        existing.setApprovalResult(ReservationStatus.REJECTED.name());
        existing.setApprovalTime(LocalDateTime.now());
        existing.setApproverId(record.getApproverId());
        existing.setRemark(record.getRemark());
        ApprovalRecord saved = approvalRecordRepository.save(existing);

        Reservation reservation = reservationRepository.findById(record.getReservationId())
                .orElseThrow(() -> new BusinessException("预约不存在"));
        reservation.setStatus(ReservationStatus.REJECTED.name());
        reservationRepository.save(reservation);

        // 发送通知给预约人
        Notification notification = new Notification();
        notification.setUserId(reservation.getUserId());
        notification.setTitle("预约审批被拒绝");
        String remarkInfo = (record.getRemark() != null && !record.getRemark().isEmpty())
            ? "，原因：" + record.getRemark() : "";
        notification.setContent("您的预约「" + reservation.getPurpose() + "」已被拒绝" + remarkInfo + "。");
        notification.setType("REJECTED");
        notification.setReservationId(reservation.getId());
        notification.setTenantId(reservation.getTenantId());
        notificationRepository.save(notification);

        return saved;
    }
}
