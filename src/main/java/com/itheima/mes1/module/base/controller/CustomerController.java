package com.itheima.mes1.module.base.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Customer;
import com.itheima.mes1.module.base.mapper.CustomerMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/base/customer")
public class CustomerController {

    private final ServiceImpl<CustomerMapper, Customer> service;

    public CustomerController(CustomerMapper mapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("base:customer:list")
    @GetMapping
    public Result<PageResult<Customer>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<Customer>()
                .like(StrUtil.isNotBlank(keyword), Customer::getName, keyword)
                .or().like(StrUtil.isNotBlank(keyword), Customer::getCode, keyword)
                .orderByDesc(Customer::getCreateTime);
        Page<Customer> result = service.page(new Page<>(page, pageSize), w);
        result.setTotal(service.count(w));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("base:customer:add")
    @PostMapping
    public Result<?> add(@RequestBody Customer c) { service.save(c); return Result.ok(); }

    @RequirePermission("base:customer:edit")
    @PutMapping
    public Result<?> update(@RequestBody Customer c) { service.updateById(c); return Result.ok(); }

    @RequirePermission("base:customer:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.removeById(id); return Result.ok(); }
}
