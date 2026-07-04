package com.itheima.mes1.module.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.MailService;
import com.itheima.mes1.module.bi.entity.BiReportConfig;
import com.itheima.mes1.module.bi.mapper.BiReportConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BiScheduleService {

    @Autowired private BiReportConfigMapper configMapper;
    @Autowired private BiExportService exportService;
    @Autowired private BiAlertService alertService;
    @Autowired private MailService mailService;

    /** 每小时检查定时报表（整点过5分触发） */
    @Scheduled(cron = "0 5 * * * ?")
    public void checkScheduledReports() {
        List<BiReportConfig> configs = configMapper.selectList(
                new LambdaQueryWrapper<BiReportConfig>().eq(BiReportConfig::getStatus, 1));
        for (BiReportConfig cfg : configs) {
            LocalDateTime last = cfg.getLastRunTime();
            if (last == null || last.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                try {
                    mailService.sendHtml(cfg.getRecipients(),
                            "【造易MES】定时报表 - " + cfg.getName(),
                            "<p>" + cfg.getName() + " 已生成，请查收附件。</p>");
                    cfg.setLastRunTime(LocalDateTime.now());
                    configMapper.updateById(cfg);
                    log.info("定时报表已发送: {}", cfg.getName());
                } catch (Exception e) {
                    log.error("定时报表发送失败: {} - {}", cfg.getName(), e.getMessage());
                }
            }
        }
    }

    /** 每小时自动扫描告警 */
    @Scheduled(cron = "0 30 * * * ?")
    public void autoAlertScan() {
        log.info("开始自动告警扫描...");
        alertService.scanAndRecord();
    }

}