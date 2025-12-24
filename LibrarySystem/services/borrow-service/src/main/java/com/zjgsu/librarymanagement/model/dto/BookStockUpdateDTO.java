package com.zjgsu.librarymanagement.model.dto;

import lombok.Data;

@Data
public class BookStockUpdateDTO {
    private Long bookId;
    private Integer availableCopies; // 新的可用副本数
    private String operation; // 操作类型：BORROW, RETURN

    // 方便构造的方法
    public static BookStockUpdateDTO borrow(Long bookId, Integer currentCopies) {
        BookStockUpdateDTO dto = new BookStockUpdateDTO();
        dto.setBookId(bookId);
        dto.setAvailableCopies(currentCopies - 1);
        dto.setOperation("BORROW");
        return dto;
    }

    public static BookStockUpdateDTO returnBook(Long bookId, Integer currentCopies) {
        BookStockUpdateDTO dto = new BookStockUpdateDTO();
        dto.setBookId(bookId);
        dto.setAvailableCopies(currentCopies + 1);
        dto.setOperation("RETURN");
        return dto;
    }
}