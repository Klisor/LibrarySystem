package com.zjgsu.librarymanagement.model.dto;

import lombok.Data;

@Data
public class BorrowStatsDTO {
    private Long totalUsers;
    private Long totalBooks;
    private Long totalBorrowed;
    private Long totalOverdue;
    private Long todayBorrowed;
    private Long todayReturned;
}
