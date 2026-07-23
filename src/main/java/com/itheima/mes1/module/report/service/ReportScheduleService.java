package com.itheima.mes1.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.report.entity.ReportRecord;
import com.itheima.mes1.module.report.entity.ReportSchedule;
import com.itheima.mes1.module.report.mapper.ReportScheduleMapper;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ReportScheduleService {

    @Autowired private ReportScheduleMapper scheduleMapper;
    @Autowired private ReportGenerateService generateService;
    @Autowired private SysUserMapper userMapper;
    @Autowired private MessageSender messageSender;

    /** 列出用户的定时配置 */
    public List<ReportSchedule> listByUser(Long userId) {
        return scheduleMapper.selectList(
                new LambdaQueryWrapper<ReportSchedule>()
                        .eq(ReportSchedule::getUserId, userId)
                        .orderByDesc(ReportSchedule::getCreateTime));
    }

    /** 创建定时配置 */
    public ReportSchedule create(ReportSchedule schedule) {
        schedule.setStatus(schedule.getStatus() != null ? schedule.getStatus() : 1);
        schedule.setIncludeSelf(schedule.getIncludeSelf() != null ? schedule.getIncludeSelf() : 1);
        scheduleMapper.insert(schedule);
        return schedule;
    }

    /** 更新定时配置 */
    public void update(ReportSchedule schedule) {
        scheduleMapper.updateById(schedule);
    }

    /** 删除定时配置 */
    public void delete(Long id, Long userId) {
        ReportSchedule s = scheduleMapper.selectById(id);
        if (s != null && s.getUserId().equals(userId)) {
            scheduleMapper.deleteById(id);
        }
    }

    /**
     * 每分钟扫描 — 检查到期任务并发送报表邮件
     */
    @Transactional
    public void scanAndSend() {
        List<ReportSchedule> schedules = scheduleMapper.selectAllActive();
        LocalDateTime now = LocalDateTime.now();

        for (ReportSchedule schedule : schedules) {
            try {
                CronExpression cron = CronExpression.parse(schedule.getCronExpr());
                LocalDateTime base = schedule.getLastRunTime() != null
                        ? schedule.getLastRunTime()
                        : (schedule.getCreateTime() != null ? schedule.getCreateTime() : now);
                LocalDateTime next = cron.next(base);
                if (next == null || next.isAfter(now)) continue;

                // 到期 → 生成报表
                log.info("定时报表到期: scheduleId={} type={} cron={}", schedule.getId(), schedule.getReportType(), schedule.getCronExpr());
                ReportRecord record = generateService.generate(
                        schedule.getReportType(), "自动", schedule.getUserId());

                // 收集收件人
                List<String> toList = new ArrayList<>();
                if (schedule.getIncludeSelf() != null && schedule.getIncludeSelf() == 1) {
                    SysUser user = userMapper.selectById(schedule.getUserId());
                    if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                        toList.add(user.getEmail().trim());
                    }
                }
                if (schedule.getRecipients() != null && !schedule.getRecipients().isBlank()) {
                    Arrays.stream(schedule.getRecipients().split(","))
                            .map(String::trim).filter(e -> !e.isEmpty())
                            .forEach(toList::add);
                }
                if (toList.isEmpty()) {
                    log.warn("定时报表无收件人: scheduleId={}", schedule.getId());
                    schedule.setLastRunTime(now);
                    scheduleMapper.updateById(schedule);
                    continue;
                }

                // 发送邮件
                String to = String.join(",", toList);
                String reportType = schedule.getReportType();
                String subject = "【造易MES】" + schedule.getReportTitle() + " (" + record.getTimeRange() + ")";
                String html = buildMailHtml(reportType, record);
                messageSender.sendMailWithAttachment(to, subject, html, record.getFileBytes(), record.getFileName());

                // 更新最后执行时间
                schedule.setLastRunTime(now);
                scheduleMapper.updateById(schedule);

                log.info("定时报表已发送: scheduleId={} to={} file={}", schedule.getId(), to, record.getFileName());
            } catch (Exception e) {
                log.error("定时报表执行失败: scheduleId={} type={}: {}", schedule.getId(), schedule.getReportType(), e.getMessage());
            }
        }
    }

    private String buildMailHtml(String type, ReportRecord record) {
        String typeName = switch (type) {
            case "production" -> "生产报表";
            case "sales" -> "销售报表";
            case "inventory" -> "库存报表";
            case "summary" -> "综合报表";
            default -> "报表";
        };
        return """
            <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2563eb;">造易 MES 定时报表</h2>
                <p>您好，这是您订阅的<strong>%s</strong>，详见附件。</p>
                <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">报表类型</td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">时间范围</td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">文件</td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                </table>
                <p style="color: #9ca3af; font-size: 12px;">此邮件由造易MES系统自动发送，请勿回复。</p>
            </div>
            """.formatted(typeName, typeName,
                record.getTimeRange() != null ? record.getTimeRange() : "自动",
                record.getFileName());
    }
}
