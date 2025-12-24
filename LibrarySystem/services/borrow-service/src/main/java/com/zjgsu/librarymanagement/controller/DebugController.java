//package com.zjgsu.librarymanagement.controller;
//
//import com.zjgsu.librarymanagement.client.UserServiceClient;
//import com.zjgsu.librarymanagement.client.BookServiceClient;
//import com.zjgsu.librarymanagement.model.dto.UserDTO;
//import com.zjgsu.librarymanagement.model.dto.BookDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/debug")
//@RequiredArgsConstructor
//public class DebugController {
//
//    private final UserServiceClient userServiceClient;
//    private final BookServiceClient bookServiceClient;
//
//    /**
//     * 测试用户服务连通性并检查数据
//     */
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<Map<String, Object>> testUserService(@PathVariable Long userId) {
//        try {
//            log.info("调试：测试用户服务 - userId: {}", userId);
//            UserDTO user = userServiceClient.getUserById(userId);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("timestamp", LocalDateTime.now());
//
//            if (user == null) {
//                result.put("message", "用户服务返回 null");
//            } else {
//                result.put("user", user);
//                result.put("fieldsCheck", Map.of(
//                        "id", user.getId(),
//                        "username", user.getUsername(),
//                        "email", user.getEmail(),
//                        "maxBorrowCount", user.getMaxBorrowCount(),
//                        "borrowedCount", user.getBorrowedCount(),
//                        "role", user.getRole()
//                ));
//
//                // 检查关键字段
//                StringBuilder fieldIssues = new StringBuilder();
//                if (user.getMaxBorrowCount() == null) {
//                    fieldIssues.append("maxBorrowCount 为 null; ");
//                }
//                if (user.getBorrowedCount() == null) {
//                    fieldIssues.append("borrowedCount 为 null; ");
//                }
//
//                if (fieldIssues.length() > 0) {
//                    result.put("fieldIssues", fieldIssues.toString());
//                } else {
//                    result.put("fieldIssues", "所有字段正常");
//                }
//            }
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("测试用户服务失败: {}", e.getMessage(), e);
//            Map<String, Object> error = new HashMap<>();
//            error.put("success", false);
//            error.put("error", e.getMessage());
//            error.put("errorType", e.getClass().getName());
//            error.put("timestamp", LocalDateTime.now());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//
//    /**
//     * 测试图书服务连通性并检查数据
//     */
//    @GetMapping("/book/{bookId}")
//    public ResponseEntity<Map<String, Object>> testBookService(@PathVariable Long bookId) {
//        try {
//            log.info("调试：测试图书服务 - bookId: {}", bookId);
//            BookDTO book = bookServiceClient.getBookById(bookId);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("timestamp", LocalDateTime.now());
//
//            if (book == null) {
//                result.put("message", "图书服务返回 null");
//            } else {
//                result.put("book", book);
//                result.put("fieldsCheck", Map.of(
//                        "id", book.getId(),
//                        "title", book.getTitle(),
//                        "author", book.getAuthor(),
//                        "availableCopies", book.getAvailableCopies(),
//                        "totalCopies", book.getTotalCopies()
//                ));
//
//                // 检查关键字段
//                StringBuilder fieldIssues = new StringBuilder();
//                if (book.getAvailableCopies() == null) {
//                    fieldIssues.append("availableCopies 为 null; ");
//                }
//                if (book.getTotalCopies() == null) {
//                    fieldIssues.append("totalCopies 为 null; ");
//                }
//
//                if (fieldIssues.length() > 0) {
//                    result.put("fieldIssues", fieldIssues.toString());
//                } else {
//                    result.put("fieldIssues", "所有字段正常");
//                }
//            }
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("测试图书服务失败: {}", e.getMessage(), e);
//            Map<String, Object> error = new HashMap<>();
//            error.put("success", false);
//            error.put("error", e.getMessage());
//            error.put("errorType", e.getClass().getName());
//            error.put("timestamp", LocalDateTime.now());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//
//    /**
//     * 测试更新接口
//     */
//    @PutMapping("/user/{userId}/borrow-count")
//    public ResponseEntity<Map<String, Object>> testUpdateUserBorrowCount(
//            @PathVariable Long userId,
//            @RequestBody Map<String, Integer> request) {
//        try {
//            log.info("调试：测试更新用户借阅数量 - userId: {}, request: {}", userId, request);
//
//            userServiceClient.updateUserBorrowCount(userId, request);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("message", "更新请求已发送");
//            result.put("timestamp", LocalDateTime.now());
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("测试更新用户借阅数量失败: {}", e.getMessage(), e);
//            Map<String, Object> error = new HashMap<>();
//            error.put("success", false);
//            error.put("error", e.getMessage());
//            error.put("timestamp", LocalDateTime.now());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//
//    /**
//     * 测试更新图书副本数
//     */
//    @PutMapping("/book/{bookId}/copies")
//    public ResponseEntity<Map<String, Object>> testUpdateBookCopies(
//            @PathVariable Long bookId,
//            @RequestBody Map<String, Object> request) {
//        try {
//            log.info("调试：测试更新图书副本数 - bookId: {}, request: {}", bookId, request);
//
//            bookServiceClient.updateBookCopies(bookId, request);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("message", "更新请求已发送");
//            result.put("timestamp", LocalDateTime.now());
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("测试更新图书副本数失败: {}", e.getMessage(), e);
//            Map<String, Object> error = new HashMap<>();
//            error.put("success", false);
//            error.put("error", e.getMessage());
//            error.put("timestamp", LocalDateTime.now());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//
//    /**
//     * 检查所有服务的连通性
//     */
//    @GetMapping("/services")
//    public ResponseEntity<Map<String, Object>> checkAllServices() {
//        Map<String, Object> result = new HashMap<>();
//        result.put("timestamp", LocalDateTime.now());
//
//        // 检查用户服务
//        try {
//            UserDTO testUser = userServiceClient.getUserById(1L);
//            result.put("userService", Map.of(
//                    "status", "UP",
//                    "response", testUser != null ? "有数据" : "返回null",
//                    "sampleData", testUser != null ? Map.of(
//                            "id", testUser.getId(),
//                            "username", testUser.getUsername(),
//                            "maxBorrowCount", testUser.getMaxBorrowCount()
//                    ) : "null"
//            ));
//        } catch (Exception e) {
//            result.put("userService", Map.of(
//                    "status", "DOWN",
//                    "error", e.getMessage()
//            ));
//        }
//
//        // 检查图书服务
//        try {
//            BookDTO testBook = bookServiceClient.getBookById(1L);
//            result.put("bookService", Map.of(
//                    "status", "UP",
//                    "response", testBook != null ? "有数据" : "返回null",
//                    "sampleData", testBook != null ? Map.of(
//                            "id", testBook.getId(),
//                            "title", testBook.getTitle(),
//                            "availableCopies", testBook.getAvailableCopies()
//                    ) : "null"
//            ));
//        } catch (Exception e) {
//            result.put("bookService", Map.of(
//                    "status", "DOWN",
//                    "error", e.getMessage()
//            ));
//        }
//
//        return ResponseEntity.ok(result);
//    }
//}