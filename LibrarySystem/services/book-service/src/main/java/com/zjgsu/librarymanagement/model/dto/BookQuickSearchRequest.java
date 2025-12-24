package com.zjgsu.librarymanagement.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookQuickSearchRequest {
    @NotBlank(message = "搜索关键词不能为空")
    private String q;

    @Pattern(regexp = "title|author|isbn", message = "搜索字段必须是title、author或isbn")
    private String field = "title";
}
