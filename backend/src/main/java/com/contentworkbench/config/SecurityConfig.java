package com.contentworkbench.config;

import com.contentworkbench.security.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 安全配置：注册 AuthInterceptor 拦截 /api/** 路径，校验 Session 登录态
 * 使用 SameSite Cookie + 自定义 Header 实现 CSRF 防护
 * 集成 RateLimiter 实现 IP 级别限流
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean("globalRateLimiter")
    public RateLimiter globalRateLimiter() {
        return new RateLimiter(60, 1000);
    }

    @Bean("loginRateLimiter")
    public RateLimiter loginRateLimiter() {
        return new RateLimiter(5, 30);
    }

    @Bean("platformExecutor")
    public ExecutorService platformExecutor() {
        return Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "platform-adapter");
            t.setDaemon(true);
            return t;
        });
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(globalRateLimiter()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    /**
     * 认证拦截器：检查 Session 中的 userId，验证 CSRF Token，执行全局限流
     */
    class AuthInterceptor implements HandlerInterceptor {

        private final RateLimiter rateLimiter;

        AuthInterceptor(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            // 全局 IP 限流
            String clientIp = request.getRemoteAddr();
            if (!rateLimiter.isAllowed(clientIp)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":429,\"message\":\"Too many requests\"}");
                return false;
            }

            // OPTIONS 预检请求直接放行
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            // GET/HEAD/OPTIONS 请求不需要 CSRF 验证
            String method = request.getMethod();
            if ("GET".equals(method) || "HEAD".equals(method)) {
                return validateSession(request, response);
            }

            // POST/PUT/DELETE 请求需要验证 CSRF Token
            if (!validateCsrfToken(request)) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":403,\"message\":\"Invalid CSRF token\"}");
                return false;
            }

            return validateSession(request, response);
        }

        private boolean validateSession(HttpServletRequest request, HttpServletResponse response) throws Exception {
            var session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                return false;
            }
            request.setAttribute("userId", session.getAttribute("userId"));
            return true;
        }

        private boolean validateCsrfToken(HttpServletRequest request) {
            String cookieToken = getCsrfTokenFromCookie(request);
            String headerToken = request.getHeader(CSRF_HEADER_NAME);

            if (cookieToken == null || headerToken == null) {
                return false;
            }
            return cookieToken.equals(headerToken);
        }

        private String getCsrfTokenFromCookie(HttpServletRequest request) {
            var cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (var cookie : cookies) {
                if (CSRF_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }
    }

    public static String generateCsrfToken() {
        return UUID.randomUUID().toString();
    }

    public static void setCsrfCookie(HttpServletResponse response, String token, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(CSRF_COOKIE_NAME, token)
                .path("/")
                .httpOnly(false)
                .secure(secure)
                .sameSite("Lax")
                .maxAge(86400)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearCsrfCookie(HttpServletResponse response, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(CSRF_COOKIE_NAME, "")
                .path("/")
                .httpOnly(false)
                .secure(secure)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
