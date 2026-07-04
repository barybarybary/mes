package com.itheima.mes1.module.base.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Bom;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/api/base/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequirePermission("base:product:list")
    @Operation(summary = "分页查询产品")
    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Page<Product> result = productService.pageProducts(page, pageSize, keyword, categoryId);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("base:product:list")
    @Operation(summary = "产品详情(含BOM)")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }

    @RequirePermission("base:product:add")
    @PostMapping
    public Result<?> add(@RequestBody Product product) {
        productService.save(product);
        return Result.ok();
    }

    @RequirePermission("base:product:edit")
    @PutMapping
    public Result<?> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.ok();
    }

    @RequirePermission("base:product:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.ok();
    }

    @RequirePermission("base:product:edit")
    @Operation(summary = "保存BOM")
    @PostMapping("/{id}/bom")
    public Result<?> saveBom(@PathVariable Long id, @RequestBody List<Bom> bomList) {
        productService.saveBoms(id, bomList);
        return Result.ok();
    }
}
