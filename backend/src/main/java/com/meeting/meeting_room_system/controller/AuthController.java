package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.dto.LoginDTO;
import com.meeting.meeting_room_system.dto.RegisterDTO;
import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.repository.UserRepository;
import com.meeting.meeting_room_system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@Valid @RequestBody LoginDTO loginDTO){
        String token = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
        User user = userRepository.findByUsername(loginDTO.getUsername());

        Map<String,Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("tenantId", user.getTenantId());

        Map<String,Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userInfo);

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO){
        authService.register(registerDTO.getUsername(), registerDTO.getPassword(), registerDTO.getTenantId());
        return Result.success("注册成功");
    }

    @PostMapping("/logout")
    public Map<String,Object> logout(){
        Map<String,Object> result = new HashMap<>();
        result.put("code",200);
        result.put("message","logout success");
        return result;
    }

    @GetMapping("/info")
    public Map<String,Object> info(){
        Map<String,Object> result = new HashMap<>();
        result.put("code",200);
        result.put("name","admin");
        result.put("role","admin");
        return result;
    }
}