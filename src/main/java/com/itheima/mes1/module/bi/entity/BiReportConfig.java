package com.itheima.mes1.module.bi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bi_report_config")
public class BiReportConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    @TableField("report_format")
    private String reportFormat;
    private String cronExpr;
    private String recipients;
    private Integer status;
    private LocalDateTime lastRunTime;
    private LocalDateTime createTime;
}