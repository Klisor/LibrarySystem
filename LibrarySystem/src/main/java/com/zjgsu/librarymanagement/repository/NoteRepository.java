package com.zjgsu.librarymanagement.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjgsu.librarymanagement.model.entity.Book;
import com.zjgsu.librarymanagement.model.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface NoteRepository extends JpaRepository<Note, Long>{

    List<Note> findByUserId(Long userId);

    List<Note> findByUserIdAndBookId(Long userId, Long bookId);

    List<Note> findByUserIdAndBookIdNotNull(Long userId);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    List<Note> searchNotes(@Param("userId") Long userId, @Param("keyword") String keyword);
}