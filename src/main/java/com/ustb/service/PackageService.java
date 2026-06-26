package com.ustb.service;

import com.ustb.dto.*;

import java.util.List;

public interface PackageService {
    PackageVO checkIn(CheckInRequest request, String operator);
    PackageVO pickup(Long id, String operator);
    List<PackageVO> findAll(String status);
    List<PackageVO> findOverdue();
    List<PackageVO> findAwaitingByPhone(String phone);
    List<PackageVO> findAwaitingByKeyword(String keyword);
    PageResult<PackageVO> findAllWithPage(int page, int pageSize, String keyword, String status);
    DashboardVO getDashboard();
    List<OperationLogVO> getRecentLogs(int limit);
}
