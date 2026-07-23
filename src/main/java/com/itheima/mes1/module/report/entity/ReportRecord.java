package com.itheima.mes1.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_record")
public class ReportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String reportType;
    private String timeRange;

    @TableField(select = false)
    private byte[] fileBytes;

    private String fileName;
    private Long fileSize;
    private LocalDateTime createTime;
}
