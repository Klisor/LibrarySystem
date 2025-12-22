package com.zjgsu.librarymanagement.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjgsu.librarymanagement.model.entity.Book;
import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Mapper
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>{

    List<BorrowRecord> findByUserId(Long userId);

    Optional<BorrowRecord> findById(Long id);
    List<BorrowRecord> findByBookId(Long bookId);

    List<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status);

    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);

    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.dueDate < CURRENT_TIMESTAMP")
    List<BorrowRecord> findOverdueRecords();

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrowDate >= :startDate")
    Long countBorrowedToday(@Param("startDate") LocalDateTime startDate);

    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.BorrowStatus status);

    @Select("SELECT COUNT(*) FROM borrow_records WHERE user_id = #{userId} AND status = 'BORROWED'")
    Long countBorrowingByUserId(@Param("userId") Long userId);
}