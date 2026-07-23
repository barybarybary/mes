package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_order_process")
public class WorkOrderProcess {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private Long processId;
    private Integer sort;
    private BigDecimal planQty;
    private BigDecimal finishedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private Integer status;
    private String worker;
    private Long assignedTo;
    private Long equipmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String processName;
}
