package com.contentworkbench.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.contentworkbench.common.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 全局异常处理，IllegalArgumentException → 400，Exception → 500
 * 对 SSE 请求特殊处理，避免序列化问题
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Bad request: {}", e.getMessage());
        if (isSseRequest(request)) {
            log.debug("SSE请求异常，跳过JSON响应: {}", e.getMessage());
            return null;
        }
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());
        if (isSseRequest(request)) {
            return null;
        }
        return ApiResponse.error(403, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        if (isSseRequest(request)) {
            return null;
        }
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleDataAccess(DataAccessException e, HttpServletRequest request) {
        log.error("Database error", e);
        if (isSseRequest(request)) {
            return null;
        }
        return ApiResponse.error(500, "数据库操作失败");
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ApiResponse<Void> handleTimeout(TimeoutException e, HttpServletRequest request) {
        log.warn("Request timeout: {}", e.getMessage());
        if (isSseRequest(request)) {
            return null;
        }
        return ApiResponse.error(504, "请求超时，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleInternal(Exception e, HttpServletRequest request) {
        log.error("Internal server error", e);
        if (isSseRequest(request)) {
            return null;
        }
        return ApiResponse.error(500, "Internal server error");
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
               || uri.contains("/stream");
    }
}
