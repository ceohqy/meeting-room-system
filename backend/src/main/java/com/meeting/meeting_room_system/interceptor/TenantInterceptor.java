package com.meeting.meeting_room_system.interceptor;

import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username);
            if (user != null) {
                request.setAttribute("currentUser", user);
                request.setAttribute("currentTenantId", user.getTenantId());
                request.setAttribute("currentRole", user.getRole());
                request.setAttribute("currentUserId", user.getId());
            }
        }
        return true;
    }
}
