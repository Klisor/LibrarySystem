package com.zjgsu.librarymanagement.service.impl;

import com.zjgsu.librarymanagement.exception.BusinessException;
import com.zjgsu.librarymanagement.model.dto.BookDTO;
import com.zjgsu.librarymanagement.model.dto.BookSearchRequest;
import com.zjgsu.librarymanagement.model.entity.Book;
import com.zjgsu.librarymanagement.repository.BookRepository;
import com.zjgsu.librarymanagement.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    // 分类映射
    private static final Map<Integer, String> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put(1, "文学");
        CATEGORY_MAP.put(2, "历史");
        CATEGORY_MAP.put(3, "科学");
        CATEGORY_MAP.put(4, "技术");
        CATEGORY_MAP.put(5, "教育");
        CATEGORY_MAP.put(6, "艺术");
        CATEGORY_MAP.put(7, "商业");
        CATEGORY_MAP.put(8, "健康");
        CATEGORY_MAP.put(9, "旅行");
        CATEGORY_MAP.put(10, "其他");
    }

    @Override
    public BookDTO getBookById(Long id) {
        return bookRepository.findById(id)
                .map(BookDTO::fromEntity)
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> searchBooks(BookSearchRequest request) {
        // 1. 洗参数：空串/"null" 全部转 null
        String title   = StringUtils.hasText(request.getTitle())   ? request.getTitle().trim()   : null;
        String author  = StringUtils.hasText(request.getAuthor())  ? request.getAuthor().trim()  : null;
        Integer category = (request.getCategory() != null && request.getCategory() > 0)
                ? request.getCategory() : null;

        log.debug(">>> searchBooks 洗后参数：title={}, author={}, category={}", title, author, category);

        // 2. 查库
        List<Book> books = bookRepository.findByTitleContainingAndAuthorContainingAndCategory(title, author, category);
        log.debug(">>> 命中 {} 条记录", books.size());

        // 3. 组装返回
        List<BookDTO> result = books.stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());

        log.debug(">>> 返回 {} 条 DTO", result.size());
        return result;
    }

    @Override
    public List<BookDTO> quickSearch(String keyword, String field) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("搜索关键词不能为空");
        }

        List<Book> books;
        switch (field) {
            case "title":
                books = bookRepository.findByTitleContaining(keyword);
                break;
            case "author":
                books = bookRepository.findByAuthorContaining(keyword);
                break;
            case "isbn":
                books = bookRepository.findAll().stream()
                        .filter(book -> book.getIsbn().contains(keyword))
                        .collect(Collectors.toList());
                break;
            default:
                throw new BusinessException("搜索字段必须是title、author或isbn");
        }

        return books.stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO bookDTO) {
        // 检查ISBN是否已存在
        if (existsByIsbn(bookDTO.getIsbn())) {
            throw new BusinessException("ISBN已存在");
        }

        // 验证分类ID
        if (bookDTO.getCategory() < 1 || bookDTO.getCategory() > 10) {
            throw new BusinessException("分类ID无效，必须在1-10之间");
        }

        // 创建图书
        Book book = bookDTO.toEntity();
        // 设置初始可用副本数等于总副本数
        if (book.getTotalCopies() != null && book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        Book savedBook = bookRepository.save(book);
        log.info("添加图书成功: {}", savedBook.getTitle());

        return BookDTO.fromEntity(savedBook);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));

        // 更新ISBN（如果需要）
        if (StringUtils.hasText(bookDTO.getIsbn()) && !book.getIsbn().equals(bookDTO.getIsbn())) {
            if (existsByIsbn(bookDTO.getIsbn())) {
                throw new BusinessException("ISBN已存在");
            }
            book.setIsbn(bookDTO.getIsbn());
        }

        // 更新其他字段
        if (StringUtils.hasText(bookDTO.getTitle())) {
            book.setTitle(bookDTO.getTitle());
        }
        if (StringUtils.hasText(bookDTO.getAuthor())) {
            book.setAuthor(bookDTO.getAuthor());
        }
        if (bookDTO.getPublisher() != null) {
            book.setPublisher(bookDTO.getPublisher());
        }
        if (bookDTO.getPublishYear() != null) {
            book.setPublishYear(bookDTO.getPublishYear());
        }
        if (bookDTO.getCategory() != null) {
            if (bookDTO.getCategory() < 1 || bookDTO.getCategory() > 10) {
                throw new BusinessException("分类ID无效，必须在1-10之间");
            }
            book.setCategory(bookDTO.getCategory());
        }
        if (bookDTO.getTotalCopies() != null) {
            // 计算已借出数量
            int borrowedCount = book.getTotalCopies() - book.getAvailableCopies();
            if (bookDTO.getTotalCopies() < borrowedCount) {
                throw new BusinessException("总副本数不能小于已借出的数量");
            }

            book.setTotalCopies(bookDTO.getTotalCopies());
            // 自动更新可用副本数
            book.setAvailableCopies(bookDTO.getTotalCopies() - borrowedCount);
        }
        if (bookDTO.getLocation() != null) {
            book.setLocation(bookDTO.getLocation());
        }
        if (bookDTO.getDescription() != null) {
            book.setDescription(bookDTO.getDescription());
        }
        if (bookDTO.getAvailableCopies() != null) {
            book.setAvailableCopies(bookDTO.getAvailableCopies());
        }
        Book updatedBook = bookRepository.save(book);
        log.info("更新图书成功: {}", updatedBook.getTitle());

        return BookDTO.fromEntity(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));

        // 检查图书是否已被借阅（通过可用副本数判断）
        if (book.getAvailableCopies() != book.getTotalCopies()) {
            throw new BusinessException("图书已被借阅，无法删除");
        }

        bookRepository.delete(book);
        log.info("删除图书成功: {}", book.getTitle());
    }

    @Override
    public Map<Integer, String> getCategories() {
        return CATEGORY_MAP;
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    public Long getTotalBooks() {
        return bookRepository.count();
    }

    // 新增方法：更新图书库存（供借阅服务调用）
    @Override
    @Transactional
    public void updateBookStock(BookDTO bookDTO) {
        if (bookDTO.getId() == null) {
            throw new BusinessException("图书ID不能为空");
        }

        Book book = bookRepository.findById(bookDTO.getId())
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));

        if (bookDTO.getAvailableCopies() == null) {
            throw new BusinessException("可用副本数不能为空");
        }

        if (bookDTO.getAvailableCopies() < 0) {
            throw new BusinessException("可用副本数不能小于0");
        }
        if (bookDTO.getAvailableCopies() > book.getTotalCopies()) {
            throw new BusinessException("可用副本数不能大于总副本数");
        }

        book.setAvailableCopies(bookDTO.getAvailableCopies());
        bookRepository.save(book);
        log.info("更新图书 {} 的可用副本数为: {}", book.getId(), book.getAvailableCopies());
    }
}