package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal finishedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private String sourceType;
    private String sourceNo;
    private Long sourceId;
    private Long sourceItemId;
    private Integer status;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String remark;
    @TableLogic
    private Integer deleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
    @TableField(exist = false)
    private List<WorkOrderProcess> processes;
}
