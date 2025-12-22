package com.zjgsu.librarymanagement.service.impl;

import com.zjgsu.librarymanagement.exception.BusinessException;
import com.zjgsu.librarymanagement.model.dto.BorrowDTO;
import com.zjgsu.librarymanagement.model.dto.BorrowRequest;
import com.zjgsu.librarymanagement.model.dto.BorrowStatsDTO;
import com.zjgsu.librarymanagement.model.entity.Book;
import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.repository.BookRepository;
import com.zjgsu.librarymanagement.repository.BorrowRecordRepository;
import com.zjgsu.librarymanagement.repository.UserRepository;
import com.zjgsu.librarymanagement.service.BorrowService;
import com.zjgsu.librarymanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserService userService;

    @Value("${app.borrow.default-days:30}")
    private int defaultBorrowDays;

    @Value("${app.borrow.renew-days:15}")
    private int renewDays;

    @Value("${app.borrow.max-renew-count:1}")
    private int maxRenewCount;

    @Override
    @Transactional
    public BorrowDTO borrowBook(BorrowRequest borrowRequest) {
        // 验证参数
        if (borrowRequest.getUserId() == null || borrowRequest.getBookId() == null) {
            throw new BusinessException("用户ID和图书ID不能为空");
        }

        // 获取用户和图书信息
        User user = userRepository.findById(borrowRequest.getUserId())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Book book = bookRepository.findById(borrowRequest.getBookId())
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));

        // 检查用户是否可以借书
        if (!canUserBorrow(user.getId())) {
            throw new BusinessException("用户已达到最大借阅数量");
        }

        // 检查图书是否有库存
        if (!isBookAvailable(book.getId())) {
            throw new BusinessException("图书库存不足");
        }

        // 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(user.getId());
        borrowRecord.setBookId(book.getId());
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(defaultBorrowDays));
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.BORROWED);

        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);

        // 更新用户借阅数量
        user.setBorrowedCount(user.getBorrowedCount() + 1);
        userRepository.save(user);

        // 更新图书可用副本数
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        log.info("借书成功 - 用户: {}, 图书: {}", user.getUsername(), book.getTitle());

        return createBorrowDTO(savedRecord, user, book);
    }

    @Override
    @Transactional
    public BorrowDTO returnBook(Long recordId) {
        // 获取借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));

        // 检查图书是否已归还
        if (borrowRecord.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new BusinessException("图书已归还");
        }

        // 更新借阅记录
        borrowRecord.setReturnDate(LocalDateTime.now());
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);

        BorrowRecord updatedRecord = borrowRecordRepository.save(borrowRecord);

        // 获取用户和图书信息
        User user = userRepository.findById(borrowRecord.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Book book = bookRepository.findById(borrowRecord.getBookId())
                .orElseThrow(() -> new BusinessException("图书不存在"));

        // 更新用户借阅数量
        user.setBorrowedCount(user.getBorrowedCount() - 1);
        userRepository.save(user);

        // 更新图书可用副本数
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        log.info("还书成功 - 用户: {}, 图书: {}", user.getUsername(), book.getTitle());

        return createBorrowDTO(updatedRecord, user, book);
    }

    @Override
    @Transactional
    public BorrowDTO renewBook(Long userId,Long recordId) {

        // 获取借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));

        // 检查权限（只能是借阅者本人）
//        if (!borrowRecord.getUserId().equals(userId)) {
//            throw new BusinessException(403, "无权操作此借阅记录");
//        }

        // 检查状态
        if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
            throw new BusinessException("图书已归还，不能续借");
        }

        // 检查续借次数
        if (borrowRecord.getRenewedCount() >= borrowRecord.getMaxRenewCount()) {
            throw new BusinessException("已达到最大续借次数");
        }

        // 更新借阅记录
        borrowRecord.setDueDate(borrowRecord.getDueDate().plusDays(renewDays));
        borrowRecord.setRenewedCount(borrowRecord.getRenewedCount() + 1);

        BorrowRecord updatedRecord = borrowRecordRepository.save(borrowRecord);

        log.info("续借成功 - 借阅记录ID: {}", recordId);

        BorrowDTO dto = BorrowDTO.fromEntity(updatedRecord);
        // 可以在这里添加用户名和书名信息
        return dto;
    }

    @Override
    public BorrowDTO getBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);
                    // 可以在这里添加关联的用户和图书信息
                    return dto;
                })
                .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));
    }

    @Override
    public List<BorrowDTO> getBorrowRecords(Long userId, BorrowRecord.BorrowStatus status) {
        List<BorrowRecord> records;
        if (userId != null && userId == 0L)       userId = null;
        if (status != null && "".equals(status.name())) status = null;
        System.out.println("【借阅记录】userId=" + userId + ", status=" + status);
        if (userId != null && status != null) {
            records = borrowRecordRepository.findByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            records = borrowRecordRepository.findByUserId(userId);
        } else if (status != null) {
            records = borrowRecordRepository.findByStatus(status);
        } else {
            records = borrowRecordRepository.findAll();
        }

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);
                    // 可以在这里添加关联的用户和图书信息
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowDTO> getCurrentUserBorrowRecords(Long userId,BorrowRecord.BorrowStatus status) {
        List<BorrowRecord> records;
        if (status != null) {
            records = borrowRecordRepository.findByUserIdAndStatus(userId, status);
        } else {
            records = borrowRecordRepository.findByUserId(userId);
        }

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);
                    // 可以在这里添加图书信息
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowDTO> getOverdueRecords() {
        List<BorrowRecord> records = borrowRecordRepository.findOverdueRecords();

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);
                    // 计算逾期天数
//                     long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDateTime.now());
//                     dto.setOverdueDays(overdueDays);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BorrowStatsDTO getBorrowStats() {
        BorrowStatsDTO stats = new BorrowStatsDTO();

        // 总用户数
        stats.setTotalUsers(userRepository.count());

        // 总图书数
        stats.setTotalBooks(bookRepository.count());

        // 当前借阅总数
        stats.setTotalBorrowed(borrowRecordRepository.countBorrowingByUserId(null));

        // 逾期总数
        stats.setTotalOverdue((long) borrowRecordRepository.findOverdueRecords().size());

        // 今日借阅数
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        stats.setTodayBorrowed(borrowRecordRepository.countBorrowedToday(todayStart));

        // 今日归还数（需要实现）
        // stats.setTodayReturned(...);

        return stats;
    }

    @Override
    public boolean canUserBorrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return user.getBorrowedCount() < user.getMaxBorrowCount();
    }

    @Override
    public boolean isBookAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        return book.getAvailableCopies() > 0;
    }

    // 辅助方法
    private BorrowDTO createBorrowDTO(BorrowRecord record, User user, Book book) {
        BorrowDTO dto = BorrowDTO.fromEntity(record);
        dto.setUserName(user.getUsername());
        dto.setBookTitle(book.getTitle());
        return dto;
    }
}