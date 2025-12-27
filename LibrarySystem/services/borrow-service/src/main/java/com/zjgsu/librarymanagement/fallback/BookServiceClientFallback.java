package com.zjgsu.librarymanagement.fallback;

import com.zjgsu.librarymanagement.client.BookServiceClient;
import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

@Component
@Slf4j
public class BookServiceClientFallback implements BookServiceClient {

    @Override
    public ApiResponse<BookDTO> getBookById(Long bookId,
                                            @RequestHeader("Authorization") String tk) { // 与接口签名一致
        log.warn("[BookService Fallback] 图书服务不可用，查询图书ID: {}", bookId);
        return ApiResponse.error(503, "图书服务暂时不可用，请稍后再试");
    }

    @Override
    public void updateBookStock(BookDTO bookDTO,
                                @RequestHeader("Authorization") String tk) {
        log.warn("[BookService Fallback] 图书服务不可用，无法更新图书库存，图书ID: {}", bookDTO.getId());
        // 静默失败
    }
}