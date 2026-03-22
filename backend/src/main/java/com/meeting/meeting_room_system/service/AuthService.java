package com.meeting.meeting_room_system.service;

import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.exception.BusinessException;
import com.meeting.meeting_room_system.repository.UserRepository;
import com.meeting.meeting_room_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    public void register(String username, String password, Long tenantId) {
        if (userRepository.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // 注册页面只能注册普通用户
        user.setTenantId(tenantId); // 设置用户所属租户
        userRepository.save(user);
    }
}
