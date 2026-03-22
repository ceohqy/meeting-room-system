package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.entity.Notification;
import com.meeting.meeting_room_system.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 获取当前用户的通知列表（最近20条）
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<Map<String, Object>> result = notifications.stream().limit(20).map(n -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", n.getId());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("type", n.getType());
            map.put("reservationId", n.getReservationId());
            map.put("isRead", n.getIsRead());
            map.put("createTime", n.getCreateTime() != null ? n.getCreateTime().format(FMT) : "");
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    // 获取未读数量
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return Result.success(count);
    }

    // 全部标记已读
    @PostMapping("/read-all")
    public Result<Void> readAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        notificationRepository.markAllReadByUserId(userId);
        return Result.success("已全部标记为已读", null);
    }

    // 标记单条已读
    @PostMapping("/read/{id}")
    public Result<Void> readOne(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        notificationRepository.findById(id).ifPresent(n -> {
            if (userId.equals(n.getUserId())) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
        return Result.success(null);
    }
}
