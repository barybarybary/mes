package com.itheima.mes1.module.bi.controller;

import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.bi.entity.BiAlertRecord;
import com.itheima.mes1.module.bi.entity.BiAlertRule;
import com.itheima.mes1.module.bi.service.BiAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "BI预警中心")
@RestController
@RequestMapping("/api/bi")
public class BiAlertController {

    @Autowired
    private BiAlertService alertService;

    // ========== 告警记录 ==========

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "告警列表")
    @GetMapping("/alerts")
    public Result<PageResult<BiAlertRecord>> listAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer isRead) {
        return Result.ok(alertService.listAlerts(page, pageSize, level, category, isRead));
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "未读告警数")
    @GetMapping("/alerts/count")
    public Result<Map<String, Object>> unreadCount() {
        return Result.ok(Map.of("unread", alertService.unreadCount()));
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "标记已读")
    @PutMapping("/alerts/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        alertService.markRead(id);
        return Result.ok();
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "全部已读")
    @PutMapping("/alerts/read-all")
    public Result<?> markAllRead() {
        alertService.markAllRead();
        return Result.ok();
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "手动触发告警扫描")
    @PostMapping("/alerts/scan")
    public Result<?> scan() {
        alertService.scanAndRecord();
        return Result.ok();
    }

    // ========== 告警规则 ==========

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "规则列表")
    @GetMapping("/alert-rules")
    public Result<?> listRules() {
        return Result.ok(alertService.listRules());
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "创建规则")
    @PostMapping("/alert-rules")
    public Result<BiAlertRule> createRule(@RequestBody BiAlertRule rule) {
        return Result.ok(alertService.createRule(rule));
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "编辑规则")
    @PutMapping("/alert-rules/{id}")
    public Result<BiAlertRule> updateRule(@PathVariable Long id, @RequestBody BiAlertRule rule) {
        rule.setId(id);
        return Result.ok(alertService.updateRule(rule));
    }

    @RequirePermission("bi:alert:manage")
    @Operation(summary = "删除规则")
    @DeleteMapping("/alert-rules/{id}")
    public Result<?> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return Result.ok();
    }
}