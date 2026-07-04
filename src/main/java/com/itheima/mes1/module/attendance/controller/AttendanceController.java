package com.itheima.mes1.module.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.attendance.entity.Attendance;
import com.itheima.mes1.module.attendance.mapper.AttendanceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "考勤打卡")
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @RequirePermission("attendance:view")
    @Operation(summary = "今日打卡记录")
    @GetMapping("/today")
    public Result<Map<String, Object>> today(@RequestAttribute("userId") Long userId) {
        Attendance record = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .eq(Attendance::getWorkDate, LocalDate.now()));
        Map<String, Object> data = new LinkedHashMap<>();
        if (record != null) {
            data.put("id", record.getId());
            data.put("clockInTime", record.getClockIn() != null ? record.getClockIn().toLocalTime().toString().substring(0, 5) : null);
            data.put("clockOutTime", record.getClockOut() != null ? record.getClockOut().toLocalTime().toString().substring(0, 5) : null);
            data.put("lateIn", record.getLateIn());
            data.put("earlyOut", record.getEarlyOut());
            data.put("status", record.getStatus());
        } else {
            data.put("clockInTime", null);
            data.put("clockOutTime", null);
        }
        return Result.ok(data);
    }

    @RequirePermission("attendance:view")
    @Operation(summary = "月考勤记录")
    @GetMapping
    public Result<List<Map<String, Object>>> monthly(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String month) {
        String ym = month != null ? month : LocalDate.now().toString().substring(0, 7);
        LocalDate start = LocalDate.parse(ym + "-01");
        LocalDate end = start.plusMonths(1);

        List<Attendance> list = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .ge(Attendance::getWorkDate, start)
                        .lt(Attendance::getWorkDate, end)
                        .orderByAsc(Attendance::getWorkDate));

        String[] weekDays = {"周日","周一","周二","周三","周四","周五","周六"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (Attendance a : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("date", a.getWorkDate().toString());
            item.put("weekday", weekDays[a.getWorkDate().getDayOfWeek().getValue() % 7]);
            item.put("clockInTime", a.getClockIn() != null ? a.getClockIn().toLocalTime().toString().substring(0, 5) : null);
            item.put("clockOutTime", a.getClockOut() != null ? a.getClockOut().toLocalTime().toString().substring(0, 5) : null);
            item.put("lateIn", a.getLateIn());
            item.put("earlyOut", a.getEarlyOut());
            item.put("status", a.getStatus());
            result.add(item);
        }
        return Result.ok(result);
    }

    @RequirePermission("attendance:view")
    @Operation(summary = "上班打卡")
    @PostMapping("/clock-in")
    public Result<?> clockIn(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> body) {
        Attendance record = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .eq(Attendance::getWorkDate, LocalDate.now()));
        if (record == null) {
            record = new Attendance();
            record.setUserId(userId);
            record.setWorkDate(LocalDate.now());
            record.setClockIn(LocalDateTime.now());
            record.setLateIn(LocalDateTime.now().toLocalTime().isAfter(java.time.LocalTime.of(9, 0)) ? 1 : 0);
            attendanceMapper.insert(record);
        } else if (record.getClockIn() == null) {
            record.setClockIn(LocalDateTime.now());
            record.setLateIn(LocalDateTime.now().toLocalTime().isAfter(java.time.LocalTime.of(9, 0)) ? 1 : 0);
            attendanceMapper.updateById(record);
        }
        return Result.ok();
    }

    @RequirePermission("attendance:view")
    @Operation(summary = "下班打卡")
    @PostMapping("/clock-out")
    public Result<?> clockOut(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> body) {
        Attendance record = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .eq(Attendance::getWorkDate, LocalDate.now()));
        if (record != null && record.getClockOut() == null) {
            record.setClockOut(LocalDateTime.now());
            record.setEarlyOut(LocalDateTime.now().toLocalTime().isBefore(java.time.LocalTime.of(18, 0)) ? 1 : 0);
            attendanceMapper.updateById(record);
        }
        return Result.ok();
    }
}
