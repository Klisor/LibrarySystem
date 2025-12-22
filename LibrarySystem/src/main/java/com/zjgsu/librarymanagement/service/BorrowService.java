package com.zjgsu.librarymanagement.service;

import com.zjgsu.librarymanagement.model.dto.BorrowDTO;
import com.zjgsu.librarymanagement.model.dto.BorrowRequest;
import com.zjgsu.librarymanagement.model.dto.BorrowStatsDTO;
import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BorrowService {

    // 借阅管理
    BorrowDTO borrowBook(BorrowRequest borrowRequest);

    BorrowDTO returnBook(Long recordId);

    BorrowDTO renewBook(Long userId,Long recordId);

    // 查询借阅记录
    BorrowDTO getBorrowRecordById(Long id);

    List<BorrowDTO> getBorrowRecords(Long userId, BorrowRecord.BorrowStatus status);

    List<BorrowDTO> getCurrentUserBorrowRecords(Long userId,BorrowRecord.BorrowStatus status);

    List<BorrowDTO> getOverdueRecords();

    // 统计
    BorrowStatsDTO getBorrowStats();

    // 检查
    boolean canUserBorrow(Long userId);

    boolean isBookAvailable(Long bookId);
}