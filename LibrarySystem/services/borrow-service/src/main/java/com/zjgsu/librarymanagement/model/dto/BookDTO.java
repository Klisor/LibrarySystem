package com.zjgsu.librarymanagement.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class BookDTO {
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    @Min(value = 1000, message = "出版年份不正确")
    @Max(value = 2100, message = "出版年份不正确")
    private Integer publishYear;

    @Min(value = 1, message = "分类ID必须在1-10之间")
    @Max(value = 10, message = "分类ID必须在1-10之间")
    private Integer category = 10;

    @Min(value = 1, message = "总副本数必须大于0")
    private Integer totalCopies = 1;

    @Min(value = 0, message = "可用副本数不能小于0")
    private Integer availableCopies;

    private String location;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}

