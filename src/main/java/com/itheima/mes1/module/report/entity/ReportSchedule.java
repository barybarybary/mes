package com.itheima.mes1.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_schedule")
public class ReportSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reportType;
    private String reportTitle;
    private String cronExpr;
    private String recipients;
    private Integer includeSelf;
    private Integer status;
    private LocalDateTime lastRunTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
