package com.itheima.mes1.module.report.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.common.util.RequestContextUtil;
import com.itheima.mes1.module.report.entity.ReportRecord;
import com.itheima.mes1.module.report.entity.ReportSchedule;
import com.itheima.mes1.module.report.mapper.ReportRecordMapper;
import com.itheima.mes1.module.report.service.ReportGenerateService;
import com.itheima.mes1.module.report.service.ReportScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "报表中心")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired private ReportGenerateService generateService;
    @Autowired private ReportScheduleService scheduleService;
    @Autowired private ReportRecordMapper recordMapper;

    @RequirePermission("report:manage")
    @Operation(summary = "报表记录列表")
    @GetMapping("/records")
    public Result<List<ReportRecord>> records() {
        Long userId = RequestContextUtil.currentUserId();
        return Result.ok(generateService.listByUser(userId, 50));
    }

    @RequirePermission("report:manage")
    @Operation(summary = "手动生成报表")
    @PostMapping("/generate")
    public Result<ReportRecord> generate(@RequestBody Map<String, String> body) {
        Long userId = RequestContextUtil.currentUserId();
        String reportType = body.getOrDefault("reportType", "summary");
        String timeRange = body.getOrDefault("timeRange", "本月");
        ReportRecord record = generateService.generate(reportType, timeRange, userId);
        return Result.ok(record);
    }

    @RequirePermission("report:manage")
    @Operation(summary = "下载报表 Excel")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Long userId = RequestContextUtil.currentUserId();
        ReportRecord record = recordMapper.selectWithBytes(id);
        if (record == null || !record.getUserId().equals(userId)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无权限或记录不存在\"}");
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
        response.setHeader("Content-Length", String.valueOf(record.getFileSize()));
        response.getOutputStream().write(record.getFileBytes());
        response.getOutputStream().flush();
    }

    // ==================== 定时配置 CRUD ====================

    @RequirePermission("report:manage")
    @Operation(summary = "定时配置列表")
    @GetMapping("/schedules")
    public Result<List<ReportSchedule>> schedules() {
        Long userId = RequestContextUtil.currentUserId();
        return Result.ok(scheduleService.listByUser(userId));
    }

    @RequirePermission("report:manage")
    @Operation(summary = "创建定时配置")
    @PostMapping("/schedule")
    public Result<ReportSchedule> createSchedule(@RequestBody ReportSchedule schedule) {
        schedule.setUserId(RequestContextUtil.currentUserId());
        return Result.ok(scheduleService.create(schedule));
    }

    @RequirePermission("report:manage")
    @Operation(summary = "更新定时配置")
    @PutMapping("/schedule/{id}")
    public Result<?> updateSchedule(@PathVariable Long id, @RequestBody ReportSchedule schedule) {
        schedule.setId(id);
        schedule.setUserId(RequestContextUtil.currentUserId());
        scheduleService.update(schedule);
        return Result.ok();
    }

    @RequirePermission("report:manage")
    @Operation(summary = "删除定时配置")
    @DeleteMapping("/schedule/{id}")
    public Result<?> deleteSchedule(@PathVariable Long id) {
        Long userId = RequestContextUtil.currentUserId();
        scheduleService.delete(id, userId);
        return Result.ok();
    }
}
