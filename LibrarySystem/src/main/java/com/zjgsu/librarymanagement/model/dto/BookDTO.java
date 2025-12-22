package com.zjgsu.librarymanagement.model.dto;

import com.zjgsu.librarymanagement.model.entity.Book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class BookDTO {
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    @Min(value = 1000, message = "出版年份不正确")
    @Max(value = 2100, message = "出版年份不正确")
    private Integer publishYear;

    @Min(value = 1, message = "分类ID必须在1-10之间")
    @Max(value = 10, message = "分类ID必须在1-10之间")
    private Integer category = 10;

    @Min(value = 1, message = "总副本数必须大于0")
    private Integer totalCopies = 1;

    @Min(value = 0, message = "可用副本数不能小于0")
    private Integer availableCopies;

    private String location;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 转换为Book实体
    public Book toEntity() {
        Book book = new Book();
        book.setIsbn(this.isbn);
        book.setTitle(this.title);
        book.setAuthor(this.author);
        book.setPublisher(this.publisher);
        book.setPublishYear(this.publishYear);
        book.setCategory(this.category);
        book.setTotalCopies(this.totalCopies);
        if (this.availableCopies != null) {
            book.setAvailableCopies(this.availableCopies);
        } else {
            book.setAvailableCopies(this.totalCopies);
        }
        book.setLocation(this.location);
        book.setDescription(this.description);
        return book;
    }

    // 从Book实体转换
    public static BookDTO fromEntity(Book book) {
        if (book == null) {
            return null;
        }
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublishYear(book.getPublishYear());
        dto.setCategory(book.getCategory());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setLocation(book.getLocation());
        dto.setDescription(book.getDescription());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }
}

