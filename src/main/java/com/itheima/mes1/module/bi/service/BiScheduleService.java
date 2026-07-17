package com.itheima.mes1.module.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.bi.entity.BiReportConfig;
import com.itheima.mes1.module.bi.mapper.BiReportConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BiScheduleService {

    @Autowired private BiReportConfigMapper configMapper;
    @Autowired private BiExportService exportService;
    @Autowired private BiAlertService alertService;
    @Autowired private MessageSender messageSender;

    /** 每小时检查定时报表（整点过5分触发） */
    @Scheduled(cron = "0 5 * * * ?")
    public void checkScheduledReports() {
        List<BiReportConfig> configs = configMapper.selectList(
                new LambdaQueryWrapper<BiReportConfig>().eq(BiReportConfig::getStatus, 1));
        for (BiReportConfig cfg : configs) {
            LocalDateTime last = cfg.getLastRunTime();
            if (last == null || last.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                try {
                    // 生成报表
                    byte[] report = generateReport(cfg);
                    String filename = cfg.getName() + "_" + LocalDate.now() + ".xlsx";

                    // 构造邮件正文
                    String html = """
                            <div style="max-width:520px;margin:0 auto;font-family:'PingFang SC','Microsoft YaHei',Arial,sans-serif;">
                              <div style="background:linear-gradient(135deg,#1a73e8,#4a90d9);padding:20px 24px;border-radius:10px 10px 0 0;">
                                <h2 style="color:#fff;margin:0;">&#x1F4CA; 造易 MES 定时报表</h2>
                              </div>
                              <div style="border:1px solid #e8eaed;border-top:none;padding:24px;border-radius:0 0 10px 10px;">
                                <h3 style="color:#1a73e8;margin:0 0 12px;">%s</h3>
                                <p style="color:#5f6368;line-height:1.8;">报表已自动生成，请查收附件。</p>
                                <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                                  <tr><td style="padding:8px 12px;background:#f8f9fa;color:#5f6368;">报表名称</td><td style="padding:8px 12px;">%s</td></tr>
                                  <tr><td style="padding:8px 12px;background:#f8f9fa;color:#5f6368;">报表类型</td><td style="padding:8px 12px;">%s</td></tr>
                                  <tr><td style="padding:8px 12px;background:#f8f9fa;color:#5f6368;">生成时间</td><td style="padding:8px 12px;">%s</td></tr>
                                </table>
                                <p style="font-size:12px;color:#bdc1c6;">此邮件由系统自动发送，请勿回复。</p>
                              </div>
                            </div>
                            """.formatted(cfg.getName(), cfg.getName(),
                                    typeLabel(cfg.getType()),
                                    LocalDateTime.now().toString().replace("T", " ").substring(0, 19));

                    // 通过消息队列异步发送
                    messageSender.sendMailWithAttachment(cfg.getRecipients(),
                            "【造易MES】定时报表 - " + cfg.getName(),
                            html, report, filename);

                    cfg.setLastRunTime(LocalDateTime.now());
                    configMapper.updateById(cfg);
                    log.info("定时报表已发送: {} recipients={} filename={}",
                            cfg.getName(), cfg.getRecipients(), filename);
                } catch (Exception e) {
                    log.error("定时报表发送失败: {} - {}", cfg.getName(), e.getMessage());
                }
            }
        }
    }

    /** 根据报表配置类型生成 Excel */
    private byte[] generateReport(BiReportConfig cfg) {
        return switch (cfg.getType()) {
            case "sales"      -> exportService.exportSalesExcel(null, null);
            case "inventory"  -> exportService.exportInventoryExcel();
            case "production" -> exportService.exportProductionExcel(null, null);
            default -> throw new IllegalArgumentException("未知报表类型: " + cfg.getType());
        };
    }

    private String typeLabel(String type) {
        return switch (type) {
            case "sales"      -> "销售报表";
            case "inventory"  -> "库存报表";
            case "production" -> "生产报表";
            default -> type;
        };
    }

    /** 每小时自动扫描告警 */
    @Scheduled(cron = "0 30 * * * ?")
    public void autoAlertScan() {
        log.info("开始自动告警扫描...");
        alertService.scanAndRecord();
    }

    /** 每天凌晨2点清理超过7天的已读预警 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCleanAlerts() {
        log.info("开始自动清理过期预警...");
        alertService.cleanOldAlerts(7);
    }

}