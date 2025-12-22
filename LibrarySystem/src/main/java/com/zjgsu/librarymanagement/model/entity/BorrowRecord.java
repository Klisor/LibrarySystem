package com.zjgsu.librarymanagement.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 借阅记录表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "borrow_records")
@TableName("borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @TableField("user_id")
    private Long userId;

    @Column(name = "book_id", nullable = false)
    @TableField("book_id")
    private Long bookId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "borrow_date", insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @TableField(value = "borrow_date", fill = FieldFill.INSERT)
    private LocalDateTime borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "due_date", nullable = false)
    @TableField("due_date")
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "return_date")
    @TableField("return_date")
    private LocalDateTime returnDate;

    @Column(name = "renewed_count", columnDefinition = "INT default 0")
    @TableField("renewed_count")
    private Integer renewedCount = 0;

    @Column(name = "max_renew_count", columnDefinition = "INT default 1")
    @TableField("max_renew_count")
    private Integer maxRenewCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('BORROWED','RETURNED','OVERDUE','LOST') default 'BORROWED'")
    @TableField("status")
    private BorrowStatus status = BorrowStatus.BORROWED;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 借阅状态枚举
     */
    public enum BorrowStatus {
        BORROWED, RETURNED, OVERDUE, LOST
    }
}