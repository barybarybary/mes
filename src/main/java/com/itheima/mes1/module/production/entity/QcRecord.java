package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qc_record")
public class QcRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private Long productId;
    private Long workOrderId;
    private String batchNo;
    private BigDecimal checkQty;
    private BigDecimal okQty;
    private BigDecimal ngQty;
    private Integer result;
    private String inspector;
    private LocalDate checkDate;
    private String ngDescription;
    private String disposition;
    private String remark;
    private LocalDateTime createTime;
}
