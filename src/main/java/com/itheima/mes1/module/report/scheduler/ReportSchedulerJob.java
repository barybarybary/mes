package com.itheima.mes1.module.report.scheduler;

import com.itheima.mes1.module.report.service.ReportScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 报表定时发送 — 每分钟扫描到期任务
 */
@Slf4j
@Component
public class ReportSchedulerJob {

    @Autowired
    private ReportScheduleService scheduleService;

    @Scheduled(cron = "0 * * * * *")
    public void scanSchedules() {
        try {
            scheduleService.scanAndSend();
        } catch (Exception e) {
            log.error("报表定时扫描异常: {}", e.getMessage());
        }
    }
}
