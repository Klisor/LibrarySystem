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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

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
}