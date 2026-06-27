package com.ustb.controller;

import com.ustb.common.Result;
import com.ustb.dto.PackageVO;
import com.ustb.service.PackageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final PackageService packageService;

    // 根据关键词（运单号，手机号或取件码）查询待取包裹
    @GetMapping("/packages")
    public Result<List<PackageVO>> queryByKeyword(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Result.error(400, "请输入手机号、取件码或运单号");
        }
        return Result.success(packageService.findAwaitingByKeyword(keyword));
    }

    @PutMapping("/packages/{id}/pickup")
    public Result<PackageVO> pickup(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        PackageVO vo = packageService.pickup(id, operator);
        return Result.success("取件成功", vo);
    }
}
