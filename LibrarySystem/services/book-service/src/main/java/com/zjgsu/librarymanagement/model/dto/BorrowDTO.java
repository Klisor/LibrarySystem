package com.zjgsu.librarymanagement.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    private String userName;

    private String bookTitle;

    private LocalDateTime borrowDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    private Integer renewedCount;

    private Integer maxRenewCount;

    private BorrowStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public enum BorrowStatus {
        BORROWED, RETURNED, OVERDUE, LOST
    }
}

