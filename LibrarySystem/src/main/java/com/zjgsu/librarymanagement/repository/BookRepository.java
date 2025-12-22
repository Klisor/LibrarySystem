package com.zjgsu.librarymanagement.repository;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjgsu.librarymanagement.model.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);


    @Query("select b from Book b " +
            "where (:title is null or b.title like %:title%) " +
            "and (:author is null or b.author like %:author%) " +
            "and (:category is null or b.category = :category)")
    List<Book> findByTitleContainingAndAuthorContainingAndCategory(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") Integer category);


    List<Book> findByTitleContaining(String title);

    List<Book> findByAuthorContaining(String author);

    List<Book> findByCategory(Integer category);

    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

    // MyBatis Plus 查询示例
    @Query("SELECT b FROM Book b " +
            "WHERE (:title IS NULL OR b.title LIKE CONCAT('%', :title, '%')) " +
            "AND b.availableCopies > 0")
    List<Book> searchAvailableBooksByTitle(@Param("title") String title);
}