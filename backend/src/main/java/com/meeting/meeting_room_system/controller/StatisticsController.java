package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.repository.ReservationRepository;
import com.meeting.meeting_room_system.repository.RoomRepository;
import com.meeting.meeting_room_system.repository.UserRepository;
import com.meeting.meeting_room_system.repository.EquipmentRepository;
import com.meeting.meeting_room_system.repository.ApprovalRecordRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;



    /**
     * 首页统计卡片
     */
    @GetMapping("/overview")
    public Map<String,Object> overview(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Map<String,Object> data = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate thisMonthStart = today.withDayOfMonth(1);

        if ("SUPER_ADMIN".equals(role)) {
            data.put("reservationCount", reservationRepository.count());
            data.put("roomCount", roomRepository.count());
            data.put("userCount", userRepository.count());
            data.put("equipmentCount", equipmentRepository.count());
            data.put("todayCount", reservationRepository.countByReservationDate(today));
            data.put("yesterdayCount", reservationRepository.countByReservationDate(yesterday));
            // pending 的口径以审批管理模块为准：approval_record.approval_result IS NULL
            data.put("pendingCount", approvalRecordRepository.countPending());
            // 统计页“审批通过率”卡片来自 overview 的 approvedCount/rejectedCount
            // SUPER_ADMIN 也需要返回，且口径与审批表一致，避免前端算出来永远是 0%
            data.put("approvedCount", approvalRecordRepository.countApproved());
            data.put("rejectedCount", approvalRecordRepository.countRejected());
        } else {
            data.put("reservationCount", reservationRepository.countByTenantId(tenantId));
            data.put("roomCount", roomRepository.countByTenantId(tenantId));
            data.put("userCount", userRepository.countByTenantId(tenantId));
            data.put("equipmentCount", equipmentRepository.countByTenantId(tenantId));
            data.put("todayCount", reservationRepository.countByReservationDateAndTenantId(today, tenantId));
            data.put("yesterdayCount", reservationRepository.countByReservationDateAndTenantId(yesterday, tenantId));
            // pending 的口径以审批管理模块为准：approval_record.approval_result IS NULL
            data.put("pendingCount", approvalRecordRepository.countPendingByTenantId(tenantId));
            // approved/rejected 同样以审批表为准，避免 reservation.status 出现 0/'0' 等历史/种子数据导致不一致
            data.put("rejectedCount", approvalRecordRepository.countRejectedByTenantId(tenantId));
            data.put("approvedCount", approvalRecordRepository.countApprovedByTenantId(tenantId));
        }

        return data;
    }


    /**
     * 最近7天预约趋势
     */
    @GetMapping("/trend")
    public Map<String,Object> trend(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Map<String,Object> data = new HashMap<>();
        List<String> days = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for(int i=6;i>=0;i--){
            LocalDate day = LocalDate.now().minusDays(i);
            long count = "SUPER_ADMIN".equals(role)
                ? reservationRepository.countByReservationDate(day)
                : reservationRepository.countByReservationDateAndTenantId(day, tenantId);
            days.add(day.toString());
            values.add(count);
        }

        data.put("days", days);
        data.put("values", values);
        return data;
    }


    /**
     * 会议室使用次数统计
     */
    @GetMapping("/roomUsage")
    public Map<String,Object> roomUsage(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<Object[]> list = "SUPER_ADMIN".equals(role)
            ? reservationRepository.countRoomUsage()
            : reservationRepository.countRoomUsageByTenantId(tenantId);

        List<String> rooms = new ArrayList<>();
        List<Long> usage = new ArrayList<>();
        for(Object[] obj : list){
            rooms.add(String.valueOf(obj[0]));
            usage.add(Long.valueOf(obj[1].toString()));
        }

        Map<String,Object> data = new HashMap<>();
        data.put("rooms", rooms);
        data.put("usage", usage);
        return data;
    }

    /**
     * 今日预约数量
     */
    @GetMapping("/todayReservation")
    public Map<String,Object> todayReservation(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        LocalDate today = LocalDate.now();
        long count = "SUPER_ADMIN".equals(role)
            ? reservationRepository.countByReservationDate(today)
            : reservationRepository.countByReservationDateAndTenantId(today, tenantId);

        Map<String,Object> data = new HashMap<>();
        data.put("todayReservation", count);
        return data;
    }

    /**
     * 会议室使用排行榜（TOP5，复用本月使用统计）
     */
    @GetMapping("/roomRank")
    public Map<String,Object> roomRank(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<Object[]> list = "SUPER_ADMIN".equals(role)
            ? reservationRepository.countRoomUsage()
            : reservationRepository.countRoomUsageByTenantId(tenantId);

        List<String> rooms = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            Object[] obj = list.get(i);
            rooms.add(String.valueOf(obj[0]));
            counts.add(Long.valueOf(obj[1].toString()));
        }

        Map<String,Object> data = new HashMap<>();
        data.put("rooms", rooms);
        data.put("counts", counts);
        return data;
    }

    /**
     * 设备统计
     */
    @GetMapping("/equipment")
    public Map<String,Object> equipment(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        long count = "SUPER_ADMIN".equals(role)
            ? equipmentRepository.count()
            : equipmentRepository.countByTenantId(tenantId);

        Map<String,Object> data = new HashMap<>();
        data.put("equipmentCount", count);
        return data;
    }

    /**
     * 近30天预约趋势
     */
    @GetMapping("/trend30")
    public Map<String,Object> trend30(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(29);

        List<com.meeting.meeting_room_system.entity.Reservation> list = "SUPER_ADMIN".equals(role)
            ? reservationRepository.findByDateRange(from, today)
            : reservationRepository.findByTenantIdAndDateRange(tenantId, from, today);

        Map<LocalDate, Long> countMap = new java.util.LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) countMap.put(today.minusDays(i), 0L);
        for (var r : list) countMap.merge(r.getReservationDate(), 1L, Long::sum);

        Map<String,Object> data = new HashMap<>();
        data.put("days", countMap.keySet().stream().map(LocalDate::toString).toList());
        data.put("values", new ArrayList<>(countMap.values()));
        return data;
    }

    /**
     * 时段分布
     */
    @GetMapping("/hourDist")
    public Map<String,Object> hourDist(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<Object[]> raw = "SUPER_ADMIN".equals(role)
            ? reservationRepository.countByHour()
            : reservationRepository.countByHourAndTenantId(tenantId);

        int[] hours = {8,9,10,11,12,13,14,15,16,17,18};
        Map<Integer,Long> map = new HashMap<>();
        for (Object[] o : raw) map.put(((Number)o[0]).intValue(), ((Number)o[1]).longValue());

        List<Long> values = new ArrayList<>();
        for (int h : hours) values.add(map.getOrDefault(h, 0L));

        Map<String,Object> data = new HashMap<>();
        data.put("hours", Arrays.stream(hours).mapToObj(h -> h+":00").toList());
        data.put("values", values);
        return data;
    }

    /**
     * 审批状态分布
     */
    @GetMapping("/approvalStatus")
    public Map<String,Object> approvalStatus(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        long approved, pending, rejected;
        if ("SUPER_ADMIN".equals(role)) {
            approved = approvalRecordRepository.countApproved();
            // pending 的口径以审批管理模块为准：approval_record.approval_result IS NULL
            pending  = approvalRecordRepository.countPending();
            rejected = approvalRecordRepository.countRejected();
        } else {
            approved = approvalRecordRepository.countApprovedByTenantId(tenantId);
            // pending 的口径以审批管理模块为准：approval_record.approval_result IS NULL
            pending  = approvalRecordRepository.countPendingByTenantId(tenantId);
            rejected = approvalRecordRepository.countRejectedByTenantId(tenantId);
        }

        Map<String,Object> data = new HashMap<>();
        long total = approved + pending + rejected;
        data.put("approved", approved);
        data.put("pending", pending);
        data.put("rejected", rejected);
        // 通过率后端兜底字段：避免前端整数截断/取错字段导致显示 0%
        double passRate = total == 0 ? 0D : (approved * 100.0D / total);
        data.put("passRate", passRate);
        data.put("approvalRate", passRate);
        data.put("total", total);
        return data;
    }

    /**
     * 审批管理顶部四个数字（建议审批页统一调用该接口）
     * all = pending + approved + rejected
     */
    @GetMapping("/approvalCounts")
    public Map<String,Object> approvalCounts(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        long pending;
        long approved;
        long rejected;

        if ("SUPER_ADMIN".equals(role)) {
            pending = approvalRecordRepository.countPending();
            approved = approvalRecordRepository.countApproved();
            rejected = approvalRecordRepository.countRejected();
        } else {
            pending = approvalRecordRepository.countPendingByTenantId(tenantId);
            approved = approvalRecordRepository.countApprovedByTenantId(tenantId);
            rejected = approvalRecordRepository.countRejectedByTenantId(tenantId);
        }

        Map<String,Object> data = new HashMap<>();
        data.put("all", pending + approved + rejected);
        data.put("pending", pending);
        data.put("approved", approved);
        data.put("rejected", rejected);
        return data;
    }

}