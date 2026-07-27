package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.QcStandard;
import com.itheima.mes1.module.production.mapper.QcStandardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "质检标准")
@RestController
@RequestMapping("/api/production/qc-standard")
public class QcStandardController {

    private final ServiceImpl<QcStandardMapper, QcStandard> service;
    private final QcStandardMapper mapper;

    public QcStandardController(QcStandardMapper mapper) {
        this.mapper = mapper;
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("production:qc:list")
    @Operation(summary = "质检标准列表")
    @GetMapping
    public Result<List<QcStandard>> list(@RequestParam(required = false) Long productId) {
        LambdaQueryWrapper<QcStandard> w = new LambdaQueryWrapper<QcStandard>()
                .eq(productId != null, QcStandard::getProductId, productId)
                .orderByAsc(QcStandard::getSortOrder);
        List<QcStandard> list = mapper.selectWithNames();
        if (productId != null) {
            list = list.stream().filter(s -> productId.equals(s.getProductId())).toList();
        }
        return Result.ok(list);
    }

    @RequirePermission("production:qc:add")
    @Operation(summary = "新增质检标准")
    @PostMapping
    public Result<?> add(@RequestBody QcStandard qs) {
        service.save(qs);
        return Result.ok();
    }

    @RequirePermission("production:qc:edit")
    @Operation(summary = "更新质检标准")
    @PutMapping
    public Result<?> update(@RequestBody QcStandard qs) {
        service.updateById(qs);
        return Result.ok();
    }

    @RequirePermission("production:qc:delete")
    @Operation(summary = "删除质检标准")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
