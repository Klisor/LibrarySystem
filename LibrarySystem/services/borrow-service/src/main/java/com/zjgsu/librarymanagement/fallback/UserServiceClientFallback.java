package com.zjgsu.librarymanagement.fallback;

import com.zjgsu.librarymanagement.client.UserServiceClient;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<UserDTO> getUserById(Long userId,
                                            @RequestHeader("Authorization") String tk) { // 注意：参数和返回值已修正
        log.warn("[UserService Fallback] 用户服务不可用，查询用户ID: {}", userId);
        // 返回一个包含错误信息的降级响应
        return ApiResponse.error(503, "用户服务暂时不可用，请稍后再试");
        // 注意：这里假设你的 ApiResponse 有类似 .error() 的静态方法
        // 如果没有，你需要用构造方法创建一个失败的响应
    }

    @Override
    public void updateUserBorrowCount(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, Integer> request,
            @RequestHeader("Authorization") String tk
    ) {
        log.warn("[UserService Fallback] 用户服务不可用，无法更新用户 {} 的借阅数量", userId);
        // 更新操作失败，静默处理或记录日志，通常不对外抛出异常打断主流程
    }
}