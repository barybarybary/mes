package com.itheima.mes1.module.bi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bi_alert_record")
public class BiAlertRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private String title;
    private String content;
    private String level;
    @TableField("is_read")
    private Integer isRead;
    private LocalDateTime createTime;
}