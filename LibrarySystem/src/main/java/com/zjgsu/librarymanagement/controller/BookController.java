package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.model.dto.BookSearchRequest;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.BookService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.Tools;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final JwtUtil jwtUtil;
    private final Tools tools;
    /**
     * 获取图书列表（前端分页）
     */
    @GetMapping
    public ApiResponse<List<BookDTO>> getBooks(@Validated BookSearchRequest searchRequest) {
        List<BookDTO> books = bookService.searchBooks(searchRequest);
        return ApiResponse.success(books);
    }

    /**
     * 获取图书详情
     */
    @GetMapping("/{id}")
    public ApiResponse<BookDTO> getBook(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk) {
        BookDTO book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    /**
     * 添加图书（管理员）
     */
    @PostMapping
    public ApiResponse<BookDTO> addBook(@RequestHeader("Authorization") String tk,
                                        @Valid @RequestBody BookDTO bookDTO
                                        ) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        BookDTO addedBook = bookService.addBook(bookDTO);
        return ApiResponse.success("图书添加成功", addedBook);
    }

    /**
     * 更新图书信息（管理员）
     */
    @PutMapping("/{id}")
    public ApiResponse<BookDTO> updateBook(
            @PathVariable Long id,
            @Validated @RequestBody BookDTO bookDTO,
            @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        return ApiResponse.success("更新成功", updatedBook);
    }

    /**
     * 删除图书（管理员）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk) {
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        bookService.deleteBook(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 快速搜索图书
     */
    @GetMapping("/search")
    public ApiResponse<List<BookDTO>> quickSearch(
            @RequestHeader("Authorization") String tk,
            @RequestParam String q,
            @RequestParam(defaultValue = "title") String field) {

        List<BookDTO> books = bookService.quickSearch(q, field);
        return ApiResponse.success(books);
    }

    /**
     * 获取所有图书分类
     */
    @GetMapping("/categories")
    public ApiResponse<Map<Integer, String>> getCategories(@RequestHeader("Authorization") String tk) {
        Map<Integer, String> categories = bookService.getCategories();
        return ApiResponse.success(categories);
    }
}