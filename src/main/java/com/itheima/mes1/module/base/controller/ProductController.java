package com.itheima.mes1.module.base.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Bom;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.entity.Supplier;
import com.itheima.mes1.module.base.mapper.SupplierMapper;
import com.itheima.mes1.module.base.service.ProductService;
import com.itheima.mes1.module.base.vo.ProductVO;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/api/base/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private SupplierMapper supplierMapper;

    @RequirePermission("base:product:list")
    @Operation(summary = "分页查询产品")
    @GetMapping
    public Result<PageResult<ProductVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Page<Product> result = productService.pageProducts(page, pageSize, keyword, categoryId);
        // 批量查询库存
        List<Long> productIds = result.getRecords().stream().map(Product::getId).collect(Collectors.toList());
        java.util.Map<Long, Integer> stockMap = new java.util.HashMap<>();
        if (!productIds.isEmpty()) {
            for (java.util.Map<String, Object> row : inventoryMapper.sumQuantityByProductIds(productIds)) {
                Long pid = ((Number) row.get("product_id")).longValue();
                Integer qty = ((Number) row.get("stock")).intValue();
                stockMap.put(pid, qty);
            }
        }
        final java.util.Map<Long, Integer> finalStockMap = stockMap;
        // 批量查询供应商名称
        java.util.Map<Long, String> supplierMap = new java.util.HashMap<>();
        for (Product p : result.getRecords()) {
            if (p.getSupplierId() != null) supplierMap.put(p.getSupplierId(), null);
        }
        if (!supplierMap.isEmpty()) {
            for (Supplier s : supplierMapper.selectBatchIds(supplierMap.keySet())) {
                supplierMap.put(s.getId(), s.getName());
            }
        }
        List<ProductVO> voList = result.getRecords().stream()
                .map(p -> { ProductVO vo = toVO(p); vo.setStockQuantity(finalStockMap.getOrDefault(p.getId(), 0));
                    vo.setSupplierName(supplierMap.get(p.getSupplierId())); return vo; })
                .collect(Collectors.toList());
        return Result.ok(new PageResult<>(voList, result.getTotal(), page, pageSize));
    }

    @RequirePermission("base:product:list")
    @Operation(summary = "产品详情(含BOM)")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        ProductVO vo = toVO(productService.getDetail(id));
        if (vo != null && vo.getSupplierId() != null) {
            Supplier s = supplierMapper.selectById(vo.getSupplierId());
            if (s != null) vo.setSupplierName(s.getName());
        }
        return Result.ok(vo);
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
    @Operation(summary = "切换产品上下架状态")
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Product product = productService.getById(id);
        if (product == null) return Result.fail("产品不存在");
        Integer newStatus = body.get("status");
        if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
            return Result.fail("状态值无效，仅支持 0(下架) 或 1(上架)");
        }
        product.setStatus(newStatus);
        productService.updateById(product);
        return Result.ok(newStatus == 1 ? "已上架" : "已下架");
    }

    @RequirePermission("base:product:edit")
    @Operation(summary = "保存BOM")
    @PostMapping("/{id}/bom")
    public Result<?> saveBom(@PathVariable Long id, @RequestBody List<Bom> bomList) {
        productService.saveBoms(id, bomList);
        return Result.ok();
    }

    private ProductVO toVO(Product p) {
        if (p == null) return null;
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }
}
