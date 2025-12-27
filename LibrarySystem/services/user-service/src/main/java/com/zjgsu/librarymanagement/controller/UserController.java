package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.RegisterRequest;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.model.dto.UserUpdateRequest;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.UserService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.Tools;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Tools tools;

    // 改用字段注入
    @Value("${server.port}")
    private String serverPort;

    @Value("${INSTANCE_ID:user-service}")
    private String instanceId;
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest registerrequest) {
        UserDTO registeredUser = userService.register(registerrequest);
        return ApiResponse.success("注册成功", registeredUser);
    }

    /**
     * 管理员添加用户
     */
    @PostMapping
    public ApiResponse<UserDTO> addUser(@Valid @RequestBody UserDTO userDTO,
                                        @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        UserDTO addedUser = userService.addUser(userDTO);
        return ApiResponse.success("用户添加成功", addedUser);
    }

    /**
     * 获取用户列表（前端分页）
     */
    @GetMapping
    public ApiResponse<List<UserDTO>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) User.UserRole role,
            @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        List<UserDTO> users = userService.searchUsers(username, email, role);
        return ApiResponse.success(users);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk) {
        // =============== 新增日志行 ===============
        log.info("[负载均衡]-处理请求 [getUser] - 用户ID: {} | 实例: {} | 端口: {}", id, instanceId, serverPort);
        // =========================================

        if (!tools.isAdmin(tk)||tools.isSelf(id,tk)) return ApiResponse.error("无权限");
        UserDTO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable Long id,
            @Validated @RequestBody UserUpdateRequest updateRequest,
            @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)||tools.isSelf(id,tk)) return ApiResponse.error("无权限");
        UserDTO updatedUser = userService.updateUser(id, updateRequest);
        return ApiResponse.success("更新成功", updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 更新用户借阅数量（供借阅服务内部调用）
     */
    @PutMapping("/{userId}/borrow-count")
    public ResponseEntity<Void> updateUserBorrowCount(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("Authorization") String tk) {

        // =============== 负载均衡日志 ===============
        log.info("[负载均衡]-处理请求 [updateUserBorrowCount] - 用户ID: {} | 变更: {} | 实例: {} | 端口: {}",
                userId, request.get("change"), instanceId, serverPort);

        try {
            Integer change = request.get("change");
            if (change == null) {
                return ResponseEntity.badRequest().build();
            }

            userService.updateUserBorrowCount(userId, change);
            log.info("成功更新用户借阅数量 - userId: {}, change: {}", userId, change);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("更新用户借阅数量失败: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 验证内部服务 Token - 改进版
     */
    private boolean isValidInternalToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.warn("Token为空");
                return false;
            }

            // 去除可能的空白字符
            token = token.trim();

            // 检查是否以Bearer开头（兼容不同格式）
            if (!token.startsWith("Bearer ")) {
                // 尝试添加Bearer前缀
                if (token.startsWith("eyJ")) { // 如果直接是JWT token
                    token = "Bearer " + token;
                } else {
                    return false;
                }
            }

            // 从配置中读取预期的Token
            String expectedToken = System.getenv("APP_JWT_SERVICE_TOKEN");
            if (expectedToken == null || expectedToken.trim().isEmpty()) {
                log.warn("未配置APP_JWT_SERVICE_TOKEN环境变量");
                // 如果未配置，则检查Token是否有效（解码验证）
                return isValidJwtToken(token.replace("Bearer ", ""));
            }

            // 规范化expectedToken（确保有Bearer前缀）
            if (!expectedToken.startsWith("Bearer ")) {
                expectedToken = "Bearer " + expectedToken;
            }

            // 比较Token（可以宽松一些，比如只比较主体部分）
            return token.equals(expectedToken);

        } catch (Exception e) {
            log.error("Token验证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证JWT Token是否有效
     */
    private boolean isValidJwtToken(String token) {
        try {
            // 使用JwtUtil验证Token
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }
}