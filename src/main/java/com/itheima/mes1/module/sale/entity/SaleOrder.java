package com.itheima.mes1.module.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sale_order")
public class SaleOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long customerId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private Integer status;
    private BigDecimal totalAmount;
    private String remark;
    @TableLogic
    private Integer deleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 0=未付款 1=已付款（模拟支付） */
    private Integer paid;

    /** join 结果 — 仅内部使用，API 不暴露 */
    @TableField(exist = false)
    private String customerName;
}
