package com.itheima.mes1.module.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.bi.entity.BiAlertRecord;
import com.itheima.mes1.module.bi.entity.BiAlertRule;
import com.itheima.mes1.module.bi.mapper.BiAlertRecordMapper;
import com.itheima.mes1.module.bi.mapper.BiAlertRuleMapper;
import com.itheima.mes1.module.bi.mapper.BiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BiAlertService {

    @Autowired private BiMapper biMapper;
    @Autowired private BiAlertRuleMapper ruleMapper;
    @Autowired private BiAlertRecordMapper recordMapper;
    @Autowired private MessageSender messageSender;

    // ==================== 告警规则 CRUD ====================

    public List<BiAlertRule> listRules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<BiAlertRule>().orderByDesc(BiAlertRule::getCreateTime));
    }

    public BiAlertRule createRule(BiAlertRule rule) {
        ruleMapper.insert(rule);
        return rule;
    }

    public BiAlertRule updateRule(BiAlertRule rule) {
        ruleMapper.updateById(rule);
        return rule;
    }

    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
    }

    // ==================== 告警记录 ====================

    public PageResult<BiAlertRecord> listAlerts(int page, int pageSize, String level, String category, Integer isRead) {
        LambdaQueryWrapper<BiAlertRecord> qw = new LambdaQueryWrapper<>();
        if (level != null && !level.isEmpty()) qw.eq(BiAlertRecord::getLevel, level);
        if (isRead != null) qw.eq(BiAlertRecord::getIsRead, isRead);
        qw.orderByDesc(BiAlertRecord::getCreateTime);

        Page<BiAlertRecord> result = recordMapper.selectPage(new Page<>(page, pageSize), qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public long unreadCount() {
        return recordMapper.selectCount(
                new LambdaQueryWrapper<BiAlertRecord>().eq(BiAlertRecord::getIsRead, 0));
    }

    public void markRead(Long id) {
        BiAlertRecord record = new BiAlertRecord();
        record.setId(id);
        record.setIsRead(1);
        recordMapper.updateById(record);
    }

    public void markAllRead() {
        List<BiAlertRecord> unread = recordMapper.selectList(
                new LambdaQueryWrapper<BiAlertRecord>().eq(BiAlertRecord::getIsRead, 0));
        for (BiAlertRecord r : unread) {
            r.setIsRead(1);
            recordMapper.updateById(r);
        }
    }

    // ==================== 告警扫描 ====================

    public void scanAndRecord() {
        List<Map<String, Object>> lowStock = biMapper.scanLowStock();
        for (Map<String, Object> row : lowStock) {
            saveAlert("stock", "库存低于安全线",
                    row.get("product_name") + " 在 " + row.get("warehouse_name") + " 仅剩 " + row.get("quantity"),
                    "warning");
        }

        List<Map<String, Object>> overdue = biMapper.scanOverdueOrders();
        for (Map<String, Object> row : overdue) {
            saveAlert("order", "订单超期未交付",
                    "订单 " + row.get("order_no") + "(" + row.get("customer_name") + ") 交期 " + row.get("delivery_date") + " 已超期",
                    "critical");
        }

        List<Map<String, Object>> ngList = biMapper.scanQcNgRate();
        for (Map<String, Object> row : ngList) {
            saveAlert("quality", "质检不合格率超标",
                    row.get("product_name") + " 近30天不合格率 " + row.get("ng_rate") + "%",
                    "warning");
        }

        // 通过消息队列广播未读数到所有 SSE 客户端
        messageSender.sendEvent("alert.scan", Map.of("unread", unreadCount()));
    }

    private void saveAlert(String category, String title, String content, String level) {
        long exists = recordMapper.selectCount(
                new LambdaQueryWrapper<BiAlertRecord>()
                        .eq(BiAlertRecord::getTitle, title)
                        .likeRight(BiAlertRecord::getContent, content.substring(0, Math.min(30, content.length())))
                        .ge(BiAlertRecord::getCreateTime, LocalDateTime.now().minusDays(7)));
        if (exists > 0) return;

        BiAlertRecord record = new BiAlertRecord();
        record.setCategory(category);
        record.setTitle(title);
        record.setContent(content);
        record.setLevel(level);
        record.setIsRead(0);
        recordMapper.insert(record);
    }
}