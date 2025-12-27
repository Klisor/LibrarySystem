package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.RegisterRequest;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.model.dto.UserUpdateRequest;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.UserService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.Tools;
import jakarta.servlet.http.HttpServletRequest;
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

    @Value("${server.port}")
    private String serverPort;

    @Value("${INSTANCE_ID:user-service}")
    private String instanceId;

    /**
     * 网关验证方法
     */
    private boolean isFromGateway(HttpServletRequest request) {
        // 方法1：检查特定头部（网关可以设置）
        String gatewayHeader = request.getHeader("X-Gateway-Request");
        if ("true".equals(gatewayHeader)) {
            return true;
        }

        // 方法2：检查来源IP（如果是本地开发环境，可以放宽）
        String remoteAddr = request.getRemoteAddr();
        log.debug("请求来源IP: {}", remoteAddr);

        // 本地开发环境允许直接访问
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr)) {
            return true;
        }

        // 方法3：根据环境配置决定是否严格检查
        String environment = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("dev".equals(environment) || "docker".equals(environment)) {
            log.debug("开发环境，允许直接访问");
            return true;
        }

        log.warn("非网关访问被拒绝，来源IP: {}", remoteAddr);
        return false;
    }

    /**
     * 用户注册 - 白名单，不需要网关验证
     */
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDTO registeredUser = userService.register(registerRequest);
        return ApiResponse.success("注册成功", registeredUser);
    }

    /**
     * 管理员添加用户
     */
    @PostMapping
    public ApiResponse<UserDTO> addUser(@Valid @RequestBody UserDTO userDTO,
                                        @RequestHeader("Authorization") String tk,
                                        HttpServletRequest request) {
        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

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
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        List<UserDTO> users = userService.searchUsers(username, email, role);
        return ApiResponse.success(users);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk,
                                        HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        log.info("[负载均衡]-处理请求 [getUser] - 用户ID: {} | 实例: {} | 端口: {}", id, instanceId, serverPort);

        if (!tools.isAdmin(tk) && !tools.isSelf(id, tk)) return ApiResponse.error("无权限");
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
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!tools.isAdmin(tk) && !tools.isSelf(id, tk)) return ApiResponse.error("无权限");
        UserDTO updatedUser = userService.updateUser(id, updateRequest);
        return ApiResponse.success("更新成功", updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk,
                                        HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

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
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            log.warn("非网关访问被拒绝，来源: {}", request.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }

        log.info("[负载均衡]-处理请求 [updateUserBorrowCount] - 用户ID: {} | 变更: {} | 实例: {} | 端口: {}",
                userId, requestBody.get("change"), instanceId, serverPort);

        try {
            Integer change = requestBody.get("change");
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
}