package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.BorrowDTO;
import com.zjgsu.librarymanagement.model.dto.BorrowRequest;
import com.zjgsu.librarymanagement.model.dto.BorrowStatsDTO;
import com.zjgsu.librarymanagement.model.entity.BorrowRecord;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.BorrowService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.Tools;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final JwtUtil jwtUtil;
    private final Tools tools;

    /**
     * 网关验证方法
     */
    private boolean isFromGateway(HttpServletRequest request) {
        // 方法1：检查特定头部（网关可以设置）
        String gatewayHeader = request.getHeader("X-Gateway-Request");
        if ("true".equals(gatewayHeader)) {
            return true;
        }

        // 方法2：检查来源IP（如果是本地开发环境，可以放宽）
        String remoteAddr = request.getRemoteAddr();
        log.debug("请求来源IP: {}", remoteAddr);

        // 本地开发环境允许直接访问
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr)) {
            return true;
        }

        // 方法3：根据环境配置决定是否严格检查
        String environment = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("dev".equals(environment) || "docker".equals(environment)) {
            log.debug("开发环境，允许直接访问");
            return true;
        }

        log.warn("非网关访问被拒绝，来源IP: {}", remoteAddr);
        return false;
    }

    /* ===================== 借书（管理员） ===================== */
    @PostMapping
    public ApiResponse<BorrowDTO> borrowBook(@Valid @RequestBody BorrowRequest borrowRequest,
                                             @RequestHeader("Authorization") String tk,
                                             HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");

        BorrowDTO dto = borrowService.borrowBook(borrowRequest);
        return ApiResponse.success("借书成功", dto);
    }

    /* ===================== 还书（管理员） ===================== */
    @PostMapping("/{recordId}/return")
    public ApiResponse<BorrowDTO> returnBook(@PathVariable Long recordId,
                                             @RequestHeader("Authorization") String tk,
                                             HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");

        BorrowDTO dto = borrowService.returnBook(recordId);
        return ApiResponse.success("还书成功", dto);
    }

    /* ===================== 续借（借阅者本人） ===================== */
    @PostMapping("/{recordId}/renew")
    public ApiResponse<BorrowDTO> renewBook(@PathVariable Long recordId,
                                            @RequestHeader("Authorization") String tk,
                                            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");

        Long userId=jwtUtil.getUserIdFromToken(tk);
        BorrowDTO dto = borrowService.renewBook(userId,recordId);
        return ApiResponse.success("续借成功", dto);
    }

    /* ===================== 借阅记录列表 ===================== */
    @GetMapping("/records")
    public ApiResponse<List<BorrowDTO>> getBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BorrowRecord.BorrowStatus status,
            @RequestHeader("Authorization") String tk,
            HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");

        List<BorrowDTO> records;
        if (tools.isAdmin(tk))  {
            // 只有管理员能查别人记录
            records = borrowService.getBorrowRecords(userId, status);
        } else {
            // 查自己
            userId =jwtUtil.getUserIdFromToken(tk);
            records = borrowService.getCurrentUserBorrowRecords(userId, status);
        }
        return ApiResponse.success(records);
    }

    /* ===================== 单条记录详情 ===================== */
    @GetMapping("/records/{id}")
    public ApiResponse<BorrowDTO> getBorrowRecord(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String tk,
                                                  HttpServletRequest request) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");
        Long userId=jwtUtil.getUserIdFromToken(tk);
        jwtUtil.getRoleFromToken(tk);
        BorrowDTO dto;
        if(tools.isAdmin(tk)){
            dto = borrowService.getBorrowRecordById(id);
        }else {
            dto = borrowService.getBorrowRecordById(id);
            if(userId!=dto.getUserId()){
                return ApiResponse.error("不可查阅他人记录");
            }
        }
        return ApiResponse.success(dto);
    }

    /* ===================== 逾期列表（管理员） ===================== */
    @GetMapping("/overdue")
    public ApiResponse<List<BorrowDTO>> getOverdueRecords(HttpServletRequest request,
                                                          @RequestHeader("Authorization") String tk) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");

        return ApiResponse.success(borrowService.getOverdueRecords());
    }

    /* ===================== 借阅统计（管理员） ===================== */
    @GetMapping("/stats")
    public ApiResponse<BorrowStatsDTO> getBorrowStats(HttpServletRequest request,
                                                      @RequestHeader("Authorization") String tk) {

        // 网关验证
        if (!isFromGateway(request)) {
            return ApiResponse.error(403, "请通过网关访问");
        }

        if (!jwtUtil.validateToken(tk)) return ApiResponse.error("无效token");
        if (!tools.isAdmin(tk)) return ApiResponse.error("无权限");

        return ApiResponse.success(borrowService.getBorrowStats());
    }
}