package com.itheima.mes1.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体 — 审计追踪
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作类型: CREATE / UPDATE / DELETE / LOGIN / EXPORT */
    private String operation;

    /** 目标类型: WorkOrder / SaleOrder / Product 等 */
    private String targetType;

    /** 目标 ID */
    private Long targetId;

    /** 变更详情 (JSON) */
    private String detail;

    /** 客户端 IP */
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
