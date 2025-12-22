package com.zjgsu.librarymanagement.model.dto;

import com.zjgsu.librarymanagement.model.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private User.UserRole role;

    private Integer maxBorrowCount;
    private String password;
    private Integer borrowedCount;


    // 转换为User实体
    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setEmail(this.email);
        if (this.role != null) {
            user.setRole(this.role);
        }
        if (this.maxBorrowCount != null) {
            user.setMaxBorrowCount(this.maxBorrowCount);
        }
        if (this.password!=null ){
            user.setPassword(password);
        }
        return user;
    }

    // 从User实体转换
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setMaxBorrowCount(user.getMaxBorrowCount());
        dto.setBorrowedCount(user.getBorrowedCount());
        return dto;
    }
}

