package com.itheima.mes1.module.bi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bi_alert_rule")
public class BiAlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String metric;
    private String operator;
    private java.math.BigDecimal threshold;
    private String level;
    private Integer status;
    private LocalDateTime createTime;
}