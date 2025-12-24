package com.zjgsu.librarymanagement.model.dto;

import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
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

    private BorrowRecord.BorrowStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 转换为BorrowRecord实体
    public BorrowRecord toEntity() {
        BorrowRecord record = new BorrowRecord();
        record.setUserId(this.userId);
        record.setBookId(this.bookId);
        return record;
    }

    // 从BorrowRecord实体转换
    public static BorrowDTO fromEntity(BorrowRecord record) {
        if (record == null) {
            return null;
        }
        BorrowDTO dto = new BorrowDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setBookId(record.getBookId());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setRenewedCount(record.getRenewedCount());
        dto.setMaxRenewCount(record.getMaxRenewCount());
        dto.setStatus(record.getStatus());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
}

