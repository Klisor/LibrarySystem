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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    // 改用字段注入
    @Value("${server.port}")
    private String serverPort;

    @Value("${INSTANCE_ID:user-service}")
    private String instanceId;

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

        // =============== 新增日志行 ===============
        log.info("[负载均衡]-处理请求 [getBookById] - 图书ID: {} | 实例: {} | 端口: {}", id, instanceId, serverPort);
        // =========================================

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

    // 新增端点：更新图书库存（供借阅服务内部调用）
    @PostMapping("/update-stock")
    public ResponseEntity<Void> updateBookStock(
            @RequestBody BookDTO bookDTO,
            @RequestHeader("Authorization") String tk) {

        // =============== 负载均衡日志 ===============
        log.info("[负载均衡]-处理请求 [updateBookStock] - 图书ID: {} | 实例: {} | 端口: {}",
                bookDTO.getId(), instanceId, serverPort);

        try {
            bookService.updateBookStock(bookDTO);
            log.info("更新图书库存成功 - 图书ID: {}, 可用副本数: {}",
                    bookDTO.getId(), bookDTO.getAvailableCopies());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("更新图书库存失败: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 添加权限验证方法（根据您的实际情况调整）
    private boolean isValidAdminToken(String token) {
        // 方法1：直接使用您已有的tools
        // return tools.isAdmin(token);

        // 方法2：简化验证，只检查Token是否有效
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }

        // 检查是否是特定的服务Token
        String expectedToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJpZCI6NiwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzY2MjM4NDg0LCJleHAiOjE4NTI2Mzg0ODR9.ARaGr0dOujE3vd6t5eJXCcckGFrOb3l6jAEVmIRcN4Y";
        return expectedToken.equals(token);
    }
}