package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.LoginRequest;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.response.LoginResponse;
import com.zjgsu.librarymanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ApiResponse.success(response);
    }

}