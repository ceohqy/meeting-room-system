package com.meeting.meeting_room_system.util;

import java.util.*;

public class PermissionUtil {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // 超级管理员权限
        ROLE_PERMISSIONS.put("SUPER_ADMIN", new HashSet<>(Arrays.asList(
            "tenant:create", "tenant:list", "tenant:update", "tenant:delete",
            "user:manage", "user:assignRole",
            "conferenceRoom:viewAll", "conferenceRoom:add", "conferenceRoom:update", "conferenceRoom:delete",
            "reservation:viewAll", "reservation:approve", "dashboard:view"
        )));

        // 租户管理员权限
        ROLE_PERMISSIONS.put("ADMIN", new HashSet<>(Arrays.asList(
            "user:manage", "conferenceRoom:add", "conferenceRoom:update",
            "conferenceRoom:delete", "conferenceRoom:view",
            "reservation:approve", "reservation:viewAll", "dashboard:view"
        )));

        // 普通用户权限
        ROLE_PERMISSIONS.put("USER", new HashSet<>(Arrays.asList(
            "conferenceRoom:view", "reservation:apply", "reservation:viewOwn"
        )));
    }

    public static boolean hasPermission(String role, String permission) {
        Set<String> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }
}
