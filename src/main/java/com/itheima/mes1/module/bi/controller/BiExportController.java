package com.itheima.mes1.module.bi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.bi.entity.BiReportConfig;
import com.itheima.mes1.module.bi.mapper.BiReportConfigMapper;
import com.itheima.mes1.module.bi.service.BiExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "BI报表导出")
@RestController
@RequestMapping("/api/bi")
public class BiExportController {

    @Autowired private BiExportService exportService;
    @Autowired private BiReportConfigMapper configMapper;

    @RequirePermission("bi:export")
    @Operation(summary = "导出销售报表")
    @GetMapping("/export/sales")
    public ResponseEntity<byte[]> exportSales(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        byte[] data = exportService.exportSalesExcel(year, month);
        String filename = "sales_report_" + LocalDate.now() + ".xlsx";
        return buildResponse(data, filename);
    }

    @RequirePermission("bi:export")
    @Operation(summary = "导出库存报表")
    @GetMapping("/export/inventory")
    public ResponseEntity<byte[]> exportInventory(@RequestParam(defaultValue = "excel") String format) {
        byte[] data = exportService.exportInventoryExcel();
        String filename = "inventory_report_" + LocalDate.now() + ".xlsx";
        return buildResponse(data, filename);
    }

    @RequirePermission("bi:export")
    @Operation(summary = "导出生产报表")
    @GetMapping("/export/production")
    public ResponseEntity<byte[]> exportProduction(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        byte[] data = exportService.exportProductionExcel(year, month);
        String filename = "production_report_" + LocalDate.now() + ".xlsx";
        return buildResponse(data, filename);
    }

    // ========== 定时报表 CRUD ==========

    @RequirePermission("bi:schedule:manage")
    @Operation(summary = "定时报表列表")
    @GetMapping("/schedule")
    public Result<List<BiReportConfig>> listSchedules() {
        return Result.ok(configMapper.selectList(
                new LambdaQueryWrapper<BiReportConfig>().orderByDesc(BiReportConfig::getCreateTime)));
    }

    @RequirePermission("bi:schedule:manage")
    @Operation(summary = "创建定时报表")
    @PostMapping("/schedule")
    public Result<BiReportConfig> createSchedule(@RequestBody BiReportConfig config) {
        configMapper.insert(config);
        return Result.ok(config);
    }

    @RequirePermission("bi:schedule:manage")
    @Operation(summary = "更新定时报表")
    @PutMapping("/schedule/{id}")
    public Result<BiReportConfig> updateSchedule(@PathVariable Long id, @RequestBody BiReportConfig config) {
        config.setId(id);
        configMapper.updateById(config);
        return Result.ok(config);
    }

    @RequirePermission("bi:schedule:manage")
    @Operation(summary = "删除定时报表")
    @DeleteMapping("/schedule/{id}")
    public Result<?> deleteSchedule(@PathVariable Long id) {
        configMapper.deleteById(id);
        return Result.ok();
    }

    private ResponseEntity<byte[]> buildResponse(byte[] data, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}