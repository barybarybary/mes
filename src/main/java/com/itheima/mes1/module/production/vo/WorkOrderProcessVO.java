package com.itheima.mes1.module.production.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkOrderProcessVO {
    private Long id;
    private Long workOrderId;
    private Long processId;
    private String processName;
    private Integer sort;
    private BigDecimal planQty;
    private BigDecimal finishedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private Integer status;
    private String worker;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
}
