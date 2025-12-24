package com.zjgsu.librarymanagement.model.dto;

import lombok.Data;

@Data
public class UserBorrowUpdateDTO {
    private Long userId;
    private Integer borrowedCount; // 新的借阅数量
    private String operation; // 操作类型：BORROW, RETURN

    // 方便构造的方法
    public static UserBorrowUpdateDTO borrow(Long userId, Integer currentCount) {
        UserBorrowUpdateDTO dto = new UserBorrowUpdateDTO();
        dto.setUserId(userId);
        dto.setBorrowedCount(currentCount + 1);
        dto.setOperation("BORROW");
        return dto;
    }

    public static UserBorrowUpdateDTO returnBook(Long userId, Integer currentCount) {
        UserBorrowUpdateDTO dto = new UserBorrowUpdateDTO();
        dto.setUserId(userId);
        dto.setBorrowedCount(Math.max(0, currentCount - 1));
        dto.setOperation("RETURN");
        return dto;
    }
}