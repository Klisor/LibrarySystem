package com.zjgsu.librarymanagement.client;

import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-service", url = "${app.service.user.url}")
public interface UserServiceClient {

    /**
     * 获取用户信息 - 现在返回 ApiResponse<UserDTO>
     */
    @GetMapping("/users/{userId}")
    ApiResponse<UserDTO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 更新用户借阅数量
     */
    @PutMapping("/users/{userId}/borrow-count")
    void updateUserBorrowCount(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, Integer> request
    );
}