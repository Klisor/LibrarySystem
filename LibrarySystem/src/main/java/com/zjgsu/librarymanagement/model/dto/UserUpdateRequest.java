package com.zjgsu.librarymanagement.model.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer maxBorrowCount;
}
