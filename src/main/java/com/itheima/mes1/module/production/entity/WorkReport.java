package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("work_report")
public class WorkReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private Long workOrderProcessId;
    private Long productId;
    private Long processId;
    private String worker;
    private BigDecimal quantity;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private BigDecimal workHours;
    private LocalDate reportDate;
    private String remark;
    private LocalDateTime createTime;
}
