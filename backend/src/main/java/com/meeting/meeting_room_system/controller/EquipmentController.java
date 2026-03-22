package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.entity.Equipment;
import com.meeting.meeting_room_system.repository.EquipmentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) String type,
                                              HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<Equipment> list = equipmentRepository.findAll();

        // 租户隔离：非超级管理员只能看本租户设备
        if (!"SUPER_ADMIN".equals(role)) {
            list = list.stream()
                .filter(e -> tenantId != null && tenantId.equals(e.getTenantId()))
                .toList();
        }

        // 名称搜索
        if (name != null && !name.isEmpty()) {
            list = list.stream()
                .filter(e -> e.getName() != null && e.getName().contains(name))
                .toList();
        }

        // 类型筛选
        if (type != null && !type.isEmpty()) {
            list = list.stream()
                .filter(e -> type.equals(e.getType()))
                .toList();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        return Result.success(data);
    }

    @PostMapping("/add")
    public Result<Equipment> add(@RequestBody Equipment equipment, HttpServletRequest request){
        Long tenantId = (Long) request.getAttribute("currentTenantId");
        equipment.setTenantId(tenantId); // 自动注入，不信任前端
        return Result.success(equipmentRepository.save(equipment));
    }

    @PutMapping("/update")
    public Result<Equipment> update(@RequestBody Equipment equipment, HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Equipment existing = equipmentRepository.findById(equipment.getId()).orElse(null);
        if (existing == null) {
            return Result.error(404, "设备不存在");
        }
        if (!"SUPER_ADMIN".equals(role) && !tenantId.equals(existing.getTenantId())) {
            return Result.error(403, "无权操作其他租户的设备");
        }
        equipment.setTenantId(existing.getTenantId()); // 不允许修改 tenantId
        return Result.success(equipmentRepository.save(equipment));
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        Equipment existing = equipmentRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error(404, "设备不存在");
        }
        if (!"SUPER_ADMIN".equals(role) && !tenantId.equals(existing.getTenantId())) {
            return Result.error(403, "无权操作其他租户的设备");
        }
        equipmentRepository.deleteById(id);
        return Result.success("删除成功");
    }

}
