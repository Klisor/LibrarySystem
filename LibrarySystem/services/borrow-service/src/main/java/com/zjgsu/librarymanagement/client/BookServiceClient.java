package com.zjgsu.librarymanagement.client;

import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "book-service", url = "${app.service.book.url}")
public interface BookServiceClient {

    /**
     * 获取图书信息 - 现在返回 ApiResponse<BookDTO>
     */
    @GetMapping("/books/{bookId}")
    ApiResponse<BookDTO> getBookById(@PathVariable("bookId") Long bookId);

    /**
     * 更新图书副本数
     */
    @PostMapping("/books/update-stock")
    void updateBookStock(@RequestBody BookDTO bookDTO);
}