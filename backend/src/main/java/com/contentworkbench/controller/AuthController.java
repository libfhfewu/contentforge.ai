package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.LoginRequest;
import com.contentworkbench.model.dto.RegisterRequest;
import com.contentworkbench.model.entity.User;
import com.contentworkbench.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
        session.setAttribute("userId", user.getId());
        return ApiResponse.success(user);
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        User user = userService.login(req.getEmail(), req.getPassword());
        session.setAttribute("userId", user.getId());
        return ApiResponse.success(user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success(null);
    }
}
