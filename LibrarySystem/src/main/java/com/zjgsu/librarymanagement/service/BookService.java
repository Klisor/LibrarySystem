package com.zjgsu.librarymanagement.service;
import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.model.dto.BookSearchRequest;

import java.util.List;
import java.util.Map;

public interface BookService {

    // 图书管理
    BookDTO getBookById(Long id);

    List<BookDTO> getAllBooks();

    List<BookDTO> searchBooks(BookSearchRequest searchRequest);

    List<BookDTO> quickSearch(String keyword, String field);

    BookDTO addBook(BookDTO bookDTO);

    BookDTO updateBook(Long id, BookDTO bookDTO);

    void deleteBook(Long id);

    // 分类管理
    Map<Integer, String> getCategories();

    // 检查
    boolean existsByIsbn(String isbn);

    // 统计
    Long getTotalBooks();
}