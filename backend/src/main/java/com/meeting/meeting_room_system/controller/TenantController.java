package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.annotation.RequirePermission;
import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.entity.Tenant;
import com.meeting.meeting_room_system.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @GetMapping("/list")
    public Result<List<Tenant>> list(HttpServletRequest request) {
        String role = (String) request.getAttribute("currentRole");
        // 超级管理员看全部；ADMIN/USER 只需要租户列表用于下拉选择（注册/创建用户时），返回全部但不暴露敏感信息
        if (role == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(tenantRepository.findAll());
    }

    @PostMapping("/add")
    @RequirePermission("tenant:create")
    public Result<Tenant> add(@RequestBody Tenant tenant) {
        return Result.success(tenantRepository.save(tenant));
    }

    @PutMapping("/{id}")
    @RequirePermission("tenant:update")
    public Result<Tenant> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        tenant.setId(id);
        return Result.success(tenantRepository.save(tenant));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("tenant:delete")
    public Result<String> delete(@PathVariable Long id) {
        tenantRepository.deleteById(id);
        return Result.success("删除成功");
    }
}
