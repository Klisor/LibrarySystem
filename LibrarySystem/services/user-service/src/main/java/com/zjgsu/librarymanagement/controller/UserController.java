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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @RequestHeader("Authorization") String token) {

        // 验证 Token（确保是内部服务调用）
        if (!isValidInternalToken(token)) {
            log.warn("无效的内部调用Token: {}", token);
            return ResponseEntity.status(401).build();
        }

        try {
            // 解析参数
            Integer change = request.get("change");
            if (change == null) {
                return ResponseEntity.badRequest().build();
            }

            // 调用服务层
            userService.updateUserBorrowCount(userId, change);

            log.info("成功更新用户借阅数量 - userId: {}, change: {}", userId, change);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("更新用户借阅数量失败: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 验证内部服务 Token
     */
    private boolean isValidInternalToken(String token) {
        // 从配置中读取预期的 Token（与借阅服务配置一致）
        String expectedToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJpZCI6NiwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzY2MjM4NDg0LCJleHAiOjE4NTI2Mzg0ODR9.ARaGr0dOujE3vd6t5eJXCcckGFrOb3l6jAEVmIRcN4Y";
        return expectedToken.equals(token);
    }
}