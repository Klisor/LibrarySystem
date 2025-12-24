package com.zjgsu.librarymanagement.service.impl;

import com.zjgsu.librarymanagement.client.UserServiceClient;
import com.zjgsu.librarymanagement.client.BookServiceClient;
import com.zjgsu.librarymanagement.exception.BusinessException;
import com.zjgsu.librarymanagement.model.dto.*;
import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
import com.zjgsu.librarymanagement.repository.BorrowRecordRepository;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;

    @Value("${app.borrow.default-days:30}")
    private int defaultBorrowDays;

    @Value("${app.borrow.renew-days:15}")
    private int renewDays;

    @Value("${app.borrow.max-renew-count:1}")
    private int maxRenewCount;

    @Value("${app.borrow.max-borrow-count:5}")
    private int defaultMaxBorrowCount;

    @Value("${app.service.user.url}")
    private String userServiceBaseUrl;

    @Value("${app.service.book.url}")
    private String bookServiceBaseUrl;

    @Override
    @Transactional
    public BorrowDTO borrowBook(BorrowRequest borrowRequest) {
        log.info("=== 开始处理借书请求 ===");
        log.info("请求数据: {}", borrowRequest);

        // 验证参数
        if (borrowRequest.getUserId() == null || borrowRequest.getBookId() == null) {
            log.error("参数验证失败: userId={}, bookId={}",
                    borrowRequest.getUserId(), borrowRequest.getBookId());
            throw new BusinessException("用户ID和图书ID不能为空");
        }

        Long userId = borrowRequest.getUserId();
        Long bookId = borrowRequest.getBookId();

        try {
            // 1. 调用用户服务获取用户信息
            log.info("1. 调用用户服务 - URL: {}, userId: {}",
                    userServiceBaseUrl, userId);
            UserDTO user = safeGetUserById(userId);
            if (user == null) {
                throw new BusinessException(404, "用户不存在");
            }

            log.info("用户信息详情: id={}, username={}, maxBorrowCount={}, borrowedCount={}",
                    user.getId(), user.getUsername(), user.getMaxBorrowCount(), user.getBorrowedCount());

            // 2. 检查用户是否可以借书
            log.info("2. 检查用户借阅资格");
            if (!safeCanUserBorrow(userId, user)) {
                Long currentBorrowed = borrowRecordRepository.countBorrowingByUserId(userId);
                if (currentBorrowed == null) currentBorrowed = 0L;

                int maxCount = safeGetMaxBorrowCount(user);
                throw new BusinessException(String.format(
                        "用户已达到最大借阅数量。当前借阅：%d本，最大可借：%d本",
                        currentBorrowed, maxCount
                ));
            }

            // 3. 调用图书服务获取图书信息
            log.info("3. 调用图书服务 - URL: {}, bookId: {}",
                    bookServiceBaseUrl, bookId);
            BookDTO book = safeGetBookById(bookId);
            if (book == null) {
                throw new BusinessException(404, "图书不存在");
            }

            log.info("图书信息详情: id={}, title={}, availableCopies={}, totalCopies={}",
                    book.getId(), book.getTitle(), book.getAvailableCopies(), book.getTotalCopies());

            // 4. 检查图书是否有库存
            log.info("4. 检查图书库存");
            if (!safeIsBookAvailable(book)) {
                throw new BusinessException("图书库存不足");
            }

            // 5. 检查用户是否已借阅该图书
            log.info("5. 检查用户是否已借阅该图书");
            if (borrowRecordRepository.findByUserIdAndBookIdAndStatus(userId, bookId, BorrowRecord.BorrowStatus.BORROWED)
                    .isPresent()) {
                log.warn("用户已借阅该图书: userId={}, bookId={}", userId, bookId);
                throw new BusinessException("用户已借阅该图书");
            }

            // 6. 创建借阅记录
            log.info("6. 创建借阅记录");
            BorrowRecord borrowRecord = new BorrowRecord();
            borrowRecord.setUserId(userId);
            borrowRecord.setBookId(bookId);
            borrowRecord.setBorrowDate(LocalDateTime.now());
            borrowRecord.setDueDate(LocalDateTime.now().plusDays(defaultBorrowDays));
            borrowRecord.setStatus(BorrowRecord.BorrowStatus.BORROWED);
            borrowRecord.setRenewedCount(0);
            borrowRecord.setMaxRenewCount(maxRenewCount);

            BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
            log.info("创建借阅记录成功 - recordId: {}", savedRecord.getId());

            // 7. 调用用户服务更新用户借阅数量
            log.info("7. 调用用户服务更新用户借阅数量");
            try {
                Map<String, Integer> userRequest = new HashMap<>();
                userRequest.put("change", 1);
                userServiceClient.updateUserBorrowCount(userId, userRequest);
                log.info("更新用户借阅数量成功");
            } catch (Exception e) {
                log.error("更新用户借阅数量失败: {}", e.getMessage());
                // 记录日志但不影响主流程
            }

            // 8. 调用图书服务减少可用副本数
            log.info("8. 调用图书服务减少可用副本数");
            try {
                BookDTO bookDTO = new BookDTO();
                bookDTO.setId(bookId);
                bookDTO.setAvailableCopies(book.getAvailableCopies() - 1); // 需要先获取图书信息
                bookServiceClient.updateBookStock(bookDTO);
                log.info("更新图书副本数成功");
            } catch (Exception e) {
                log.error("更新图书副本数失败: {}", e.getMessage());
                // 记录日志但不影响主流程
            }

            log.info("借书成功 - 用户: {}, 图书: {}", user.getUsername(), book.getTitle());

            return createBorrowDTO(savedRecord, user, book);
        } catch (BusinessException e) {
            log.error("借书业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("借书系统异常: {}", e.getMessage(), e);
            throw new BusinessException("借书失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BorrowDTO returnBook(Long recordId) {
        try {
            log.info("=== 开始处理还书请求 ===");
            log.info("recordId: {}", recordId);

            // 获取借阅记录
            BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));

            // 检查图书是否已归还
            if (borrowRecord.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
                throw new BusinessException("图书已归还");
            }

            Long userId = borrowRecord.getUserId();
            Long bookId = borrowRecord.getBookId();

            log.info("还书信息 - userId: {}, bookId: {}", userId, bookId);

            // 调用用户服务获取用户信息
            UserDTO user = safeGetUserById(userId);

            // 调用图书服务获取图书信息
            BookDTO book = safeGetBookById(bookId);

            // 更新借阅记录
            borrowRecord.setReturnDate(LocalDateTime.now());
            borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);

            BorrowRecord updatedRecord = borrowRecordRepository.save(borrowRecord);

            // 调用用户服务减少用户借阅数量
            try {
                Map<String, Integer> userRequest = new HashMap<>();
                userRequest.put("change", -1);
                userServiceClient.updateUserBorrowCount(userId, userRequest);
                log.info("更新用户借阅数量成功（还书）");
            } catch (Exception e) {
                log.error("还书时更新用户借阅数量失败: {}", e.getMessage());
            }

            // 调用图书服务增加可用副本数
            try {
                BookDTO bookDTO = new BookDTO();
                bookDTO.setId(bookId);
                bookDTO.setAvailableCopies(book.getAvailableCopies() - 1); // 需要先获取图书信息
                bookServiceClient.updateBookStock(bookDTO);
                log.info("更新图书副本数成功（还书）");
            } catch (Exception e) {
                log.error("还书时更新图书副本数失败: {}", e.getMessage());
            }

            log.info("还书成功 - recordId: {}", recordId);

            return createBorrowDTO(updatedRecord, user, book);
        } catch (BusinessException e) {
            log.error("还书业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("还书系统异常: {}", e.getMessage(), e);
            throw new BusinessException("还书失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BorrowDTO renewBook(Long userId, Long recordId) {
        try {
            log.info("=== 开始处理续借请求 ===");
            log.info("userId: {}, recordId: {}", userId, recordId);

            // 获取借阅记录
            BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));

            // 检查权限（只能是借阅者本人）
            if (!borrowRecord.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权操作此借阅记录");
            }

            // 检查状态
            if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
                throw new BusinessException("图书已归还，不能续借");
            }

            // 检查续借次数
            if (borrowRecord.getRenewedCount() >= borrowRecord.getMaxRenewCount()) {
                throw new BusinessException("已达到最大续借次数");
            }

            // 调用图书服务检查图书状态
            BookDTO book = safeGetBookById(borrowRecord.getBookId());
            if (book == null) {
                throw new BusinessException("图书不存在");
            }

            if (!safeIsBookAvailable(book)) {
                throw new BusinessException("图书库存不足，不能续借");
            }

            // 更新借阅记录
            borrowRecord.setDueDate(borrowRecord.getDueDate().plusDays(renewDays));
            borrowRecord.setRenewedCount(borrowRecord.getRenewedCount() + 1);

            BorrowRecord updatedRecord = borrowRecordRepository.save(borrowRecord);

            log.info("续借成功 - recordId: {}", recordId);

            // 获取用户信息
            UserDTO user = safeGetUserById(userId);

            BorrowDTO dto = BorrowDTO.fromEntity(updatedRecord);
            if (user != null) {
                dto.setUserName(user.getUsername());
            }
            if (book != null) {
                dto.setBookTitle(book.getTitle());
            }
            return dto;
        } catch (BusinessException e) {
            log.error("续借业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("续借系统异常: {}", e.getMessage(), e);
            throw new BusinessException("续借失败: " + e.getMessage());
        }
    }

    @Override
    public BorrowDTO getBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);

                    // 调用服务获取用户信息
                    try {
                        UserDTO user = safeGetUserById(record.getUserId());
                        if (user != null) {
                            dto.setUserName(user.getUsername());
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: {}", e.getMessage());
                    }

                    // 调用服务获取图书信息
                    try {
                        BookDTO book = safeGetBookById(record.getBookId());
                        if (book != null) {
                            dto.setBookTitle(book.getTitle());
                        }
                    } catch (Exception e) {
                        log.warn("获取图书信息失败: {}", e.getMessage());
                    }

                    return dto;
                })
                .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));
    }

    @Override
    public List<BorrowDTO> getBorrowRecords(Long userId, BorrowRecord.BorrowStatus status) {
        log.info("获取借阅记录 - userId: {}, status: {}", userId, status);

        List<BorrowRecord> records;
        if (userId != null && userId == 0L) userId = null;
        if (status != null && "".equals(status.name())) status = null;

        if (userId != null && status != null) {
            records = borrowRecordRepository.findByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            records = borrowRecordRepository.findByUserId(userId);
        } else if (status != null) {
            records = borrowRecordRepository.findByStatus(status);
        } else {
            records = borrowRecordRepository.findAll();
        }

        log.info("查询到记录数: {}", records.size());

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);

                    // 调用服务获取用户信息
                    try {
                        UserDTO user = safeGetUserById(record.getUserId());
                        if (user != null) {
                            dto.setUserName(user.getUsername());
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: {}", e.getMessage());
                    }

                    // 调用服务获取图书信息
                    try {
                        BookDTO book = safeGetBookById(record.getBookId());
                        if (book != null) {
                            dto.setBookTitle(book.getTitle());
                        }
                    } catch (Exception e) {
                        log.warn("获取图书信息失败: {}", e.getMessage());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowDTO> getCurrentUserBorrowRecords(Long userId, BorrowRecord.BorrowStatus status) {
        log.info("获取当前用户借阅记录 - userId: {}, status: {}", userId, status);

        List<BorrowRecord> records;
        if (status != null) {
            records = borrowRecordRepository.findByUserIdAndStatus(userId, status);
        } else {
            records = borrowRecordRepository.findByUserId(userId);
        }

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);

                    // 调用服务获取图书信息
                    try {
                        BookDTO book = safeGetBookById(record.getBookId());
                        if (book != null) {
                            dto.setBookTitle(book.getTitle());
                        }
                    } catch (Exception e) {
                        log.warn("获取图书信息失败: {}", e.getMessage());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowDTO> getOverdueRecords() {
        log.info("获取逾期记录");

        List<BorrowRecord> records = borrowRecordRepository.findOverdueRecords();

        log.info("逾期记录数: {}", records.size());

        return records.stream()
                .map(record -> {
                    BorrowDTO dto = BorrowDTO.fromEntity(record);

                    // 调用服务获取用户信息
                    try {
                        UserDTO user = safeGetUserById(record.getUserId());
                        if (user != null) {
                            dto.setUserName(user.getUsername());
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: {}", e.getMessage());
                    }

                    // 调用服务获取图书信息
                    try {
                        BookDTO book = safeGetBookById(record.getBookId());
                        if (book != null) {
                            dto.setBookTitle(book.getTitle());
                        }
                    } catch (Exception e) {
                        log.warn("获取图书信息失败: {}", e.getMessage());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BorrowStatsDTO getBorrowStats() {
        log.info("获取借阅统计");

        BorrowStatsDTO stats = new BorrowStatsDTO();

        // 当前借阅总数
        Long totalBorrowed = borrowRecordRepository.countBorrowingByUserId(null);
        stats.setTotalBorrowed(totalBorrowed != null ? totalBorrowed : 0L);

        // 逾期总数
        stats.setTotalOverdue((long) borrowRecordRepository.findOverdueRecords().size());

        // 今日借阅数
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        Long todayBorrowed = borrowRecordRepository.countBorrowedToday(todayStart);
        stats.setTodayBorrowed(todayBorrowed != null ? todayBorrowed : 0L);

        log.info("统计结果 - 当前借阅: {}, 逾期: {}, 今日借阅: {}",
                stats.getTotalBorrowed(), stats.getTotalOverdue(), stats.getTodayBorrowed());

        return stats;
    }

    @Override
    public boolean canUserBorrow(Long userId) {
        try {
            UserDTO user = safeGetUserById(userId);
            return safeCanUserBorrow(userId, user);
        } catch (Exception e) {
            log.error("检查用户借阅资格失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isBookAvailable(Long bookId) {
        try {
            BookDTO book = safeGetBookById(bookId);
            return safeIsBookAvailable(book);
        } catch (Exception e) {
            log.error("检查图书可用性失败: {}", e.getMessage());
            return false;
        }
    }

    // ============ 安全的辅助方法 ============

    /**
     * 安全地获取用户信息
     */
    private UserDTO safeGetUserById(Long userId) {
        try {
            log.debug("安全获取用户信息 - userId: {}", userId);
            ApiResponse<UserDTO> response = userServiceClient.getUserById(userId);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("获取用户信息失败 - userId: {}, response: {}", userId, response);
                return null;
            }
        } catch (Exception e) {
            log.error("安全获取用户信息异常 - userId: {}, error: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 安全地获取图书信息
     */
    private BookDTO safeGetBookById(Long bookId) {
        try {
            log.debug("安全获取图书信息 - bookId: {}", bookId);
            ApiResponse<BookDTO> response = bookServiceClient.getBookById(bookId);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("获取图书信息失败 - bookId: {}, response: {}", bookId, response);
                return null;
            }
        } catch (Exception e) {
            log.error("安全获取图书信息异常 - bookId: {}, error: {}", bookId, e.getMessage());
            return null;
        }
    }

    /**
     * 安全地检查用户是否可以借书
     */
    private boolean safeCanUserBorrow(Long userId, UserDTO user) {
        if (user == null) {
            log.warn("用户为空，不能借书 - userId: {}", userId);
            return false;
        }

        // 获取用户最大借阅数量（处理null值）
        Integer maxBorrowCount = user.getMaxBorrowCount();
        if (maxBorrowCount == null) {
            log.warn("用户 {} 的 maxBorrowCount 为 null，使用默认值 {}",
                    user.getUsername(), defaultMaxBorrowCount);
            maxBorrowCount = defaultMaxBorrowCount;
        }

        // 获取当前借阅数量
        Long currentBorrowed = borrowRecordRepository.countBorrowingByUserId(userId);
        if (currentBorrowed == null) {
            currentBorrowed = 0L;
        }

        log.debug("用户 {} 借阅资格检查: currentBorrowed={}, maxBorrowCount={}",
                user.getUsername(), currentBorrowed, maxBorrowCount);

        return currentBorrowed < maxBorrowCount;
    }

    /**
     * 安全地检查图书是否可用
     */
    private boolean safeIsBookAvailable(BookDTO book) {
        if (book == null) {
            log.warn("图书为空，不可用");
            return false;
        }

        // 处理 availableCopies 为 null 的情况
        Integer availableCopies = book.getAvailableCopies();
        if (availableCopies == null) {
            log.warn("图书 {} 的 availableCopies 为 null，视为0", book.getTitle());
            availableCopies = 0;
        }

        boolean available = availableCopies > 0;
        log.debug("图书 {} 可用性检查: availableCopies={}, available={}",
                book.getTitle(), availableCopies, available);

        return available;
    }

    /**
     * 安全地获取最大借阅数量
     */
    private int safeGetMaxBorrowCount(UserDTO user) {
        if (user == null) return defaultMaxBorrowCount;
        Integer maxCount = user.getMaxBorrowCount();
        return maxCount != null ? maxCount : defaultMaxBorrowCount;
    }

    // 辅助方法
    private BorrowDTO createBorrowDTO(BorrowRecord record, UserDTO user, BookDTO book) {
        BorrowDTO dto = BorrowDTO.fromEntity(record);
        if (user != null) {
            dto.setUserName(user.getUsername());
        }
        if (book != null) {
            dto.setBookTitle(book.getTitle());
        }
        return dto;
    }
}