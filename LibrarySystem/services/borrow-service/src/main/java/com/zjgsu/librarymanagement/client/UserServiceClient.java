package com.zjgsu.librarymanagement.client;

import com.zjgsu.librarymanagement.fallback.UserServiceClientFallback;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-service",
        fallback = UserServiceClientFallback.class)
// 移除 path = "/api"
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    ApiResponse<UserDTO> getUserById(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String tk);

    @PutMapping("/api/users/{userId}/borrow-count")
    void updateUserBorrowCount(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("Authorization") String tk);
}