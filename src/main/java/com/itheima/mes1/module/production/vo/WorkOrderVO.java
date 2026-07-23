package com.itheima.mes1.module.production.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkOrderVO {
    private Long id;
    private String orderNo;
    private Long productId;
    private String productName;
    private String productCode;
    private BigDecimal quantity;
    private BigDecimal finishedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private String sourceType;
    private String sourceNo;
    private Integer status;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<WorkOrderProcessVO> processes;
}
