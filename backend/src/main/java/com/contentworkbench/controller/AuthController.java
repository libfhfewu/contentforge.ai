package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.config.SecurityConfig;
import com.contentworkbench.model.dto.LoginRequest;
import com.contentworkbench.model.dto.RegisterRequest;
import com.contentworkbench.model.entity.User;
import com.contentworkbench.security.RateLimiter;
import com.contentworkbench.security.SecurityAuditLogger;
import com.contentworkbench.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 处理用户注册、登录、登出请求，Session 会话管理
 * 集成登录限流和安全审计日志
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final RateLimiter loginRateLimiter;
    private final SecurityAuditLogger auditLogger;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    public AuthController(UserService userService,
                          @Qualifier("loginRateLimiter") RateLimiter loginRateLimiter,
                          SecurityAuditLogger auditLogger) {
        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
        this.auditLogger = auditLogger;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest req,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        String loginKey = "login:" + clientIp;
        if (!loginRateLimiter.isAllowed(loginKey)) {
            auditLogger.logSecurityEvent(req.getEmail(), "RATE_LIMITED", "注册限流: " + clientIp);
            return ApiResponse.error(429, "Too many attempts, try again later");
        }

        try {
            User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
            request.getSession().invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("userId", user.getId());

            String csrfToken = SecurityConfig.generateCsrfToken();
            SecurityConfig.setCsrfCookie(response, csrfToken, sslEnabled);

            auditLogger.logLogin(String.valueOf(user.getId()), clientIp, true);
            return ApiResponse.success(user);
        } catch (IllegalArgumentException e) {
            auditLogger.logLogin(req.getEmail(), clientIp, false);
            throw e;
        }
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        String loginKey = "login:" + clientIp;
        if (!loginRateLimiter.isAllowed(loginKey)) {
            auditLogger.logSecurityEvent(req.getEmail(), "RATE_LIMITED", "登录限流: " + clientIp);
            return ApiResponse.error(429, "Too many login attempts, try again later");
        }

        try {
            User user = userService.login(req.getEmail(), req.getPassword());
            request.getSession().invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("userId", user.getId());

            String csrfToken = SecurityConfig.generateCsrfToken();
            SecurityConfig.setCsrfCookie(response, csrfToken, sslEnabled);

            auditLogger.logLogin(String.valueOf(user.getId()), clientIp, true);
            return ApiResponse.success(user);
        } catch (IllegalArgumentException e) {
            auditLogger.logLogin(req.getEmail(), clientIp, false);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                auditLogger.logLogout(String.valueOf(userId), request.getRemoteAddr());
            }
            session.invalidate();
        }

        SecurityConfig.clearCsrfCookie(response, sslEnabled);
        return ApiResponse.success(null);
    }

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkAuth(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            User user = userService.getById(userId);
            if (user != null) {
                return ApiResponse.success(Map.of(
                    "authenticated", true,
                    "userId", userId,
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : ""
                ));
            }
        }
        return ApiResponse.success(Map.of("authenticated", false));
    }
}
