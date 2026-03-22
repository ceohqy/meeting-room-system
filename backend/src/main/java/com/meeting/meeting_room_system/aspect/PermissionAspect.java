package com.meeting.meeting_room_system.aspect;

import com.meeting.meeting_room_system.annotation.RequirePermission;
import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.exception.BusinessException;
import com.meeting.meeting_room_system.repository.UserRepository;
import com.meeting.meeting_room_system.util.PermissionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private UserRepository userRepository;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String permission = requirePermission.value();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        if (!PermissionUtil.hasPermission(user.getRole(), permission)) {
            throw new BusinessException(403, "权限不足");
        }
    }
}
