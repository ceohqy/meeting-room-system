package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.annotation.RequirePermission;
import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.dto.RoomDTO;
import com.meeting.meeting_room_system.entity.Room;
import com.meeting.meeting_room_system.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/room")
public class MeetingRoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) Integer status,
                                              HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<Room> rooms = roomService.listAll();

        // 租户管理员只能看到本租户的会议室
        if ("ADMIN".equals(role) || "USER".equals(role)) {
            rooms = rooms.stream()
                .filter(r -> tenantId != null && tenantId.equals(r.getTenantId()))
                .toList();
        }

        // 搜索过滤
        if (name != null && !name.isEmpty()) {
            rooms = rooms.stream()
                .filter(r -> r.getRoomName() != null && r.getRoomName().contains(name))
                .toList();
        }

        // 状态过滤
        if (status != null) {
            rooms = rooms.stream()
                .filter(r -> String.valueOf(status).equals(r.getStatus()))
                .toList();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", rooms);
        data.put("total", rooms.size());
        return Result.success(data);
    }

    @PostMapping("/add")
    @RequirePermission("conferenceRoom:add")
    public Result<Room> add(@Valid @RequestBody RoomDTO roomDTO, HttpServletRequest request){
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Room room = new Room();
        room.setRoomName(roomDTO.getName());
        room.setCapacity(roomDTO.getCapacity());
        room.setLocation(roomDTO.getLocation());
        room.setEquipment(roomDTO.getEquipment());
        room.setStatus(roomDTO.getStatus() != null ? String.valueOf(roomDTO.getStatus()) : "1");
        room.setTenantId(tenantId); // 自动设置为当前用户的租户
        return Result.success("新增成功", roomService.addRoom(room));
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission("conferenceRoom:delete")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Room room = roomService.getById(id);
        if (room == null) {
            return Result.error(404, "会议室不存在");
        }
        if (!"SUPER_ADMIN".equals(role) && !tenantId.equals(room.getTenantId())) {
            return Result.error(403, "无权操作其他租户的会议室");
        }
        roomService.deleteRoom(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/update")
    @RequirePermission("conferenceRoom:update")
    public Result<Room> update(@RequestBody Room room, HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Room existing = roomService.getById(room.getId());
        if (existing == null) {
            return Result.error(404, "会议室不存在");
        }
        if (!"SUPER_ADMIN".equals(role) && !tenantId.equals(existing.getTenantId())) {
            return Result.error(403, "无权操作其他租户的会议室");
        }
        // 不允许修改 tenantId
        room.setTenantId(existing.getTenantId());
        return Result.success("修改成功", roomService.updateRoom(room));
    }
}