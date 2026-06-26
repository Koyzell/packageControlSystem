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

    @PostMapping("/packages/check-in")
    public Result<PackageVO> checkIn(@Valid @RequestBody CheckInRequest request,
                                     HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        PackageVO vo = packageService.checkIn(request, operator);
        return Result.success("包裹入库成功", vo);
    }

    @GetMapping("/packages")
    public Result<PageResult<PackageVO>> listPackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(packageService.findAllWithPage(page, pageSize, keyword, status));
    }

    @GetMapping("/packages/overdue")
    public Result<List<PackageVO>> listOverdue() {
        return Result.success(packageService.findOverdue());
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(packageService.getDashboard());
    }

    @GetMapping("/logs")
    public Result<List<OperationLogVO>> logs() {
        return Result.success(packageService.getRecentLogs(20));
    }

    @PutMapping("/packages/{id}/pickup")
    public Result<PackageVO> pickup(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        PackageVO vo = packageService.pickup(id, operator);
        return Result.success("取件成功", vo);
    }
}
