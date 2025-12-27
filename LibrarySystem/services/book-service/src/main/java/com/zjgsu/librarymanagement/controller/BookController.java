package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.model.dto.BookSearchRequest;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.BookService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.Tools;
import jakarta.servlet.http.HttpServletRequest;
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

    @Value("${server.port}")
    private String serverPort;

    @Value("${INSTANCE_ID:book-service}")
    private String instanceId;

    /**
     * 网关验证方法
     */
    private boolean isFromGateway(HttpServletRequest request) {
        String gatewayHeader = request.getHeader("X-Gateway-Request");
        if ("true".equals(gatewayHeader)) {
            return true;
        }

        String remoteAddr = request.getRemoteAddr();
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr)) {
            return true;
        }

        String environment = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("dev".equals(environment) || "docker".equals(environment)) {
            return true;
        }

        log.warn("非网关访问被拒绝，来源IP: {}", remoteAddr);
        return false;
    }

    /**
     * 获取图书列表（前端分页） - 公开接口，不需要网关验证
     */
    @GetMapping
    public ApiResponse<List<BookDTO>> getBooks(@Validated BookSearchRequest searchRequest) {
        List<BookDTO> books = bookService.searchBooks(searchRequest);
        return ApiResponse.success(books);
    }

    /**
     * 获取图书详情 - 公开接口，不需要网关验证
     */
    @GetMapping("/{id}")
    public ApiResponse<BookDTO> getBook(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk,
                                        HttpServletRequest request) {

        // 如果提供了Token，需要网关验证
        if (tk != null && !tk.trim().isEmpty() && !isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        log.info("[负载均衡]-处理请求 [getBookById] - 图书ID: {} | 实例: {} | 端口: {}", id, instanceId, serverPort);

        BookDTO book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    /**
     * 添加图书（管理员）
     */
    @PostMapping
    public ApiResponse<BookDTO> addBook(@RequestHeader("Authorization") String tk,
                                        @Valid @RequestBody BookDTO bookDTO,
                                        HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

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
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        return ApiResponse.success("更新成功", updatedBook);
    }

    /**
     * 删除图书（管理员）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id,
                                        @RequestHeader("Authorization") String tk,
                                        HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");
        bookService.deleteBook(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 快速搜索图书 - 公开接口
     */
    @GetMapping("/search")
    public ApiResponse<List<BookDTO>> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "title") String field,
            @RequestHeader(value = "Authorization", required = false) String tk,
            HttpServletRequest request) {

        // 如果提供了Token，需要网关验证
        if (tk != null && !tk.trim().isEmpty() && !isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        List<BookDTO> books = bookService.quickSearch(q, field);
        return ApiResponse.success(books);
    }

    /**
     * 获取所有图书分类 - 公开接口
     */
    @GetMapping("/categories")
    public ApiResponse<Map<Integer, String>> getCategories(
            @RequestHeader(value = "Authorization", required = false) String tk,
            HttpServletRequest request) {

        // 如果提供了Token，需要网关验证
        if (tk != null && !tk.trim().isEmpty() && !isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        Map<Integer, String> categories = bookService.getCategories();
        return ApiResponse.success(categories);
    }

    /**
     * 更新图书库存（供借阅服务内部调用）
     */
    @PostMapping("/update-stock")
    public ResponseEntity<Void> updateBookStock(
            @RequestBody BookDTO bookDTO,
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ResponseEntity.status(403).build();
        }

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
}