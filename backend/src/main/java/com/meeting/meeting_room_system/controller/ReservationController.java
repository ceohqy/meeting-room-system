package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.dto.CreateReservationDTO;
import com.meeting.meeting_room_system.entity.Reservation;
import com.meeting.meeting_room_system.entity.Room;
import com.meeting.meeting_room_system.repository.RoomRepository;
import com.meeting.meeting_room_system.service.ReservationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @PostMapping("/add")
    public Result<Reservation> addReservation(@Valid @RequestBody CreateReservationDTO dto, HttpServletRequest request){
        Long tenantId = (Long) request.getAttribute("currentTenantId");
        Long currentUserId = (Long) request.getAttribute("currentUserId");

        Reservation reservation = new Reservation();
        reservation.setUserId(currentUserId); // 从token取，不信任前端
        reservation.setRoomId(dto.getRoomId());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setApplicant(dto.getApplicant());
        reservation.setAttendees(dto.getAttendees());
        reservation.setPurpose(dto.getPurpose());
        reservation.setTenantId(tenantId); // 从token取，不信任前端
        return Result.success("预约成功", reservationService.createReservation(reservation));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listReservation(@RequestParam(required = false) String status,
                                                       HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        List<Reservation> list = reservationService.listAll();

        if ("USER".equals(role)) {
            // 普通用户只能看自己的预约（同时校验 tenantId 防止跨租户）
            list = list.stream()
                .filter(r -> currentUserId != null && currentUserId.equals(r.getUserId())
                          && tenantId != null && tenantId.equals(r.getTenantId()))
                .collect(Collectors.toList());
        } else if ("ADMIN".equals(role)) {
            // 租户管理员看本租户所有预约
            list = list.stream()
                .filter(r -> tenantId != null && tenantId.equals(r.getTenantId()))
                .collect(Collectors.toList());
        }

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            list = list.stream()
                .filter(r -> status.equals(r.getStatus()))
                .collect(Collectors.toList());
        }

        // 补充 roomName
        List<Long> roomIds = list.stream().map(Reservation::getRoomId).distinct().collect(Collectors.toList());
        Map<Long, String> roomNameMap = roomRepository.findAllById(roomIds).stream()
            .collect(Collectors.toMap(Room::getId, Room::getRoomName));

        List<Map<String, Object>> result = list.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("userId", r.getUserId());
            map.put("roomId", r.getRoomId());
            map.put("roomName", roomNameMap.getOrDefault(r.getRoomId(), "未知会议室"));
            map.put("reservationDate", r.getReservationDate());
            map.put("startTime", r.getStartTime());
            map.put("endTime", r.getEndTime());
            map.put("status", r.getStatus());
            map.put("applicant", r.getApplicant());
            map.put("attendees", r.getAttendees());
            map.put("purpose", r.getPurpose());
            map.put("tenantId", r.getTenantId());
            map.put("createTime", r.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/room/{roomId}")
    public Result<List<Reservation>> getRoomReservation(@PathVariable Long roomId){
        return Result.success(reservationService.listByRoomId(roomId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Reservation>> getUserReservation(@PathVariable Long userId){
        return Result.success(reservationService.listByUserId(userId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteReservation(@PathVariable Long id){
        reservationService.cancelReservation(id);
        return Result.success("删除成功", null);
    }
}