package com.zjgsu.librarymanagement.model.dto;

import lombok.Data;

@Data
public class BookSearchRequest {
    private String title;
    private String author;
    private Integer category;
    private Boolean availableOnly = false;
}
