package com.example.fishgame.controller;

import com.example.fishgame.common.ApiResponse;
import com.example.fishgame.dto.AuthRequest;
import com.example.fishgame.entity.User;
import com.example.fishgame.repository.UserRepository;
import com.example.fishgame.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody AuthRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return ApiResponse.fail("用户名和密码不能为空");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = JwtUtil.generateToken(user.getUsername());
        return ApiResponse.success("注册成功", Map.of("token", token, "username", user.getUsername()));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.fail("用户名或密码错误");
        }

        String token = JwtUtil.generateToken(user.getUsername());
        return ApiResponse.success("登录成功", Map.of("token", token, "username", user.getUsername()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
