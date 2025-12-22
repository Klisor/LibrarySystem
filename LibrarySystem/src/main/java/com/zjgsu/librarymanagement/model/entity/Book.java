package com.zjgsu.librarymanagement.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 书籍表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "books")
@TableName("books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    @TableField("isbn")
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    @TableField("title")
    private String title;

    @Column(name = "author", nullable = false, length = 255)
    @TableField("author")
    private String author;

    @Column(name = "publisher", length = 255)
    @TableField("publisher")
    private String publisher;

    @Column(name = "publish_year")
    @TableField("publish_year")
    private Integer publishYear;

    @Column(name = "category", nullable = false, columnDefinition = "TINYINT default 10")
    @TableField("category")
    private Integer category = 10;

    @Column(name = "total_copies", columnDefinition = "INT default 1")
    @TableField("total_copies")
    private Integer totalCopies = 1;

    @Column(name = "available_copies", columnDefinition = "INT default 1")
    @TableField("available_copies")
    private Integer availableCopies = 1;

    @Column(name = "location", length = 100)
    @TableField("location")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    @TableField("description")
    private String description;

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
}