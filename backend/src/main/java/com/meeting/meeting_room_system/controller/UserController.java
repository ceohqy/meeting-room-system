package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.annotation.RequirePermission;
import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.entity.Tenant;
import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.repository.TenantRepository;
import com.meeting.meeting_room_system.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    @RequirePermission("user:manage")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String username,
                                     @RequestParam(required = false) String role,
                                     HttpServletRequest request) {
        String currentRole = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        List<User> users = userRepository.findAll();

        // 租户管理员只能看到本租户的用户
        if ("ADMIN".equals(currentRole)) {
            users = users.stream()
                .filter(u -> tenantId != null && tenantId.equals(u.getTenantId()))
                .collect(Collectors.toList());
        }

        // 搜索过滤
        if (username != null && !username.isEmpty()) {
            users = users.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().contains(username))
                .collect(Collectors.toList());
        }

        if (role != null && !role.isEmpty()) {
            users = users.stream()
                .filter(u -> role.equals(u.getRole()))
                .collect(Collectors.toList());
        }

        // 查出所有涉及的租户，拼 tenantName
        List<Long> tenantIds = users.stream()
            .map(User::getTenantId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> tenantNameMap = tenantRepository.findAllById(tenantIds).stream()
            .collect(Collectors.toMap(Tenant::getId, Tenant::getName));

        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("role", u.getRole());
            map.put("tenantId", u.getTenantId());
            map.put("tenantName", u.getTenantId() != null ? tenantNameMap.getOrDefault(u.getTenantId(), "") : "");
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/current")
    public Result<User> current(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return Result.success(user);
    }

    @PostMapping("/add")
    @RequirePermission("user:manage")
    public Result<User> add(@RequestBody User user, HttpServletRequest request) {
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        // 租户管理员只能创建本租户的普通用户
        if ("ADMIN".equals(role)) {
            user.setRole("USER");
            user.setTenantId(tenantId);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Result.success(userRepository.save(user));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:manage")
    public Result<User> update(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        String currentRole = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error(404, "用户不存在");
        }
        // 租户管理员只能修改本租户用户
        if ("ADMIN".equals(currentRole) && !tenantId.equals(existing.getTenantId())) {
            return Result.error(403, "无权操作其他租户的用户");
        }
        if (user.getRealName() != null) existing.setRealName(user.getRealName());
        // 租户管理员不能修改角色和租户
        if ("SUPER_ADMIN".equals(currentRole)) {
            if (user.getRole() != null) existing.setRole(user.getRole());
            if (user.getTenantId() != null) existing.setTenantId(user.getTenantId());
        }
        return Result.success(userRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:manage")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        String currentRole = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");

        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error(404, "用户不存在");
        }
        // 租户管理员只能删除本租户用户
        if ("ADMIN".equals(currentRole) && !tenantId.equals(existing.getTenantId())) {
            return Result.error(403, "无权操作其他租户的用户");
        }
        userRepository.deleteById(id);
        return Result.success("删除成功");
    }
}
