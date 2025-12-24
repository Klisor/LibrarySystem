package com.zjgsu.librarymanagement.model.dto;

import com.zjgsu.librarymanagement.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(this.password); // 注意：实际应用中密码应该加密
        user.setEmail(this.email);
        // 设置默认值
        user.setRole(User.UserRole.USER);
        user.setMaxBorrowCount(5);
        user.setBorrowedCount(0);
        return user;
    }
}
