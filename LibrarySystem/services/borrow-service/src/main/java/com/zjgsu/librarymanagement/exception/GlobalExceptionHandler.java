package com.zjgsu.librarymanagement.exception;

import com.zjgsu.librarymanagement.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    // JWT认证异常（新添加）
    @ExceptionHandler(JwtAuthenticationException.class)
    public ApiResponse<?> handleJwtAuthenticationException(JwtAuthenticationException e) {
        log.error("JWT认证异常: {}", e.getMessage());
        return ApiResponse.unauthorized(e.getMessage());
    }

    // 未授权异常（新添加）
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<?> handleUnauthorizedException(UnauthorizedException e) {
        log.error("未授权异常: {}", e.getMessage());
        return ApiResponse.unauthorized(e.getMessage());
    }

    // 禁止访问异常（新添加）
    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<?> handleForbiddenException(ForbiddenException e) {
        log.error("禁止访问异常: {}", e.getMessage());
        return ApiResponse.forbidden(e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message);
        return ApiResponse.badRequest(message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常: {}", message);
        return ApiResponse.badRequest(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        log.error("约束违反异常: {}", message);
        return ApiResponse.badRequest(message);
    }

    // 其他异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResponse.internalError("系统异常，请稍后再试");
    }
}