package com.ustb.controller;

import com.ustb.common.Result;
import com.ustb.entity.CourierEntity;
import com.ustb.entity.ShelfEntity;
import com.ustb.exception.BusinessException;
import com.ustb.mapper.CourierMapper;
import com.ustb.mapper.ShelfMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MetaController {

    private final ShelfMapper shelfMapper;
    private final CourierMapper courierMapper;

    @GetMapping("/shelves")
    public Result<List<String>> listShelves() {
        List<String> names = shelfMapper.findAll().stream()
                .map(ShelfEntity::getName).toList();
        return Result.success(names);
    }

    @PostMapping("/shelves")
    public Result<Void> addShelf(@RequestBody String name) {
        if (name == null || name.isBlank() || name.length() > 10) {
            return Result.error(400, "货架名称不能为空且不超过10个字符");
        }
        if (shelfMapper.findByName(name) != null) {
            return Result.error(409, "该货架已存在");
        }
        shelfMapper.insert(ShelfEntity.builder().name(name).build());
        return Result.success("货架添加成功", null);
    }

    @GetMapping("/couriers")
    public Result<List<String>> listCouriers() {
        List<String> names = courierMapper.findAll().stream()
                .map(CourierEntity::getName).toList();
        return Result.success(names);
    }

    @PostMapping("/couriers")
    public Result<Void> addCourier(@RequestBody String name) {
        if (name == null || name.isBlank() || name.length() > 16) {
            return Result.error(400, "快递公司名称不能为空且不超过16个字符");
        }
        if (courierMapper.findByName(name) != null) {
            return Result.error(409, "该快递公司已存在");
        }
        courierMapper.insert(CourierEntity.builder().name(name).build());
        return Result.success("快递公司添加成功", null);
    }
}
