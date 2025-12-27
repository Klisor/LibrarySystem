package com.zjgsu.librarymanagement.client;

import com.zjgsu.librarymanagement.fallback.BookServiceClientFallback;
import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "book-service", fallback = BookServiceClientFallback.class)
// 移除 path = "/api"
public interface BookServiceClient {

    @GetMapping("/api/books/{bookId}")
    ApiResponse<BookDTO> getBookById(
            @PathVariable("bookId") Long bookId,
            @RequestHeader("Authorization") String tk);

    @PostMapping("/api/books/update-stock")
    void updateBookStock(
            @RequestBody BookDTO bookDTO,
            @RequestHeader("Authorization") String tk);
}