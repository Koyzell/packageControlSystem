package com.ustb.controller;

import com.ustb.common.Result;
import com.ustb.dto.*;
import com.ustb.service.PackageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PackageService packageService;

    // 包裹入库
    @PostMapping("/packages/check-in")
    public Result<PackageVO> checkIn(@Valid @RequestBody CheckInRequest request,
                                     HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        PackageVO vo = packageService.checkIn(request, operator);
        return Result.success("包裹入库成功", vo);
    }

    // 分页查询包裹列表，支持根据运单号 / 取件码模糊查询，筛选待取件，已取件列表
    @GetMapping("/packages")
    public Result<PageResult<PackageVO>> listPackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(packageService.findAllWithPage(page, pageSize, keyword, status));
    }

    // 查询逾期包裹
    @GetMapping("/packages/overdue")
    public Result<List<PackageVO>> listOverdue() {
        return Result.success(packageService.findOverdue());
    }

    // dashboard统计：
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(packageService.getDashboard());
    }

    // 查询最近日志
    @GetMapping("/logs")
    public Result<List<OperationLogVO>> logs() {
        return Result.success(packageService.getRecentLogs(20));
    }

    // 管理员代取包裹
    @PutMapping("/packages/{id}/pickup")
    public Result<PackageVO> pickup(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        PackageVO vo = packageService.pickup(id, operator);
        return Result.success("取件成功", vo);
    }
}
