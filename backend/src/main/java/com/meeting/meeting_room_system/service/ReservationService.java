package com.meeting.meeting_room_system.service;

import com.meeting.meeting_room_system.entity.ApprovalRecord;
import com.meeting.meeting_room_system.entity.Notification;
import com.meeting.meeting_room_system.entity.Reservation;
import com.meeting.meeting_room_system.enums.ReservationStatus;
import com.meeting.meeting_room_system.exception.BusinessException;
import com.meeting.meeting_room_system.repository.ApprovalRecordRepository;
import com.meeting.meeting_room_system.repository.NotificationRepository;
import com.meeting.meeting_room_system.repository.ReservationRepository;
import com.meeting.meeting_room_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getStartTime().isAfter(reservation.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        if (reservation.getReservationDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new BusinessException("预约日期不能是过去时间");
        }

        List<Reservation> conflicts = reservationRepository.findConflict(
                reservation.getRoomId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段已被预约");
        }

        reservation.setStatus(ReservationStatus.PENDING.name());
        reservation.setCreateTime(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);

        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setReservationId(saved.getId());
        approvalRecord.setApprovalResult(ReservationStatus.PENDING.name());
        approvalRecordRepository.save(approvalRecord);

        // 给本租户所有 ADMIN 发新预约待审批通知
        if (saved.getTenantId() != null) {
            userRepository.findByTenantIdAndRole(saved.getTenantId(), "ADMIN").forEach(admin -> {
                Notification n = new Notification();
                n.setUserId(admin.getId());
                n.setTitle("新预约待审批");
                n.setContent("用户「" + saved.getApplicant() + "」提交了新的预约申请，会议用途：" + saved.getPurpose() + "，请及时审批。");
                n.setType("PENDING");
                n.setReservationId(saved.getId());
                n.setTenantId(saved.getTenantId());
                notificationRepository.save(n);
            });
        }

        return saved;
    }

    public List<Reservation> listAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> listByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    public List<Reservation> listByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Transactional
    public void cancelReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new BusinessException(404, "预约不存在");
        }
        reservationRepository.deleteById(id);
    }
}
