package com.zjgsu.librarymanagement.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "users")
@TableName("users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @TableField("username")
    private String username;

    @Column(name = "password", nullable = false)
    @TableField("password")
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @TableField("email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "ENUM('ADMIN','USER') default 'USER'")
    @TableField("role")
    private UserRole role = UserRole.USER;

    @Column(name = "max_borrow_count", columnDefinition = "INT default 5")
    @TableField("max_borrow_count")
    private Integer maxBorrowCount = 5;

    @Column(name = "borrowed_count", columnDefinition = "INT default 0")
    @TableField("borrowed_count")
    private Integer borrowedCount = 0;

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

    // 逻辑删除字段
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN, USER
    }
}