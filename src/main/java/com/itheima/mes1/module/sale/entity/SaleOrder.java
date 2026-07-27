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

    /** 创建时写入，避免跨表JOIN */
    private String customerName;

    /** 收货人 */
    private String receiverName;
    /** 收货电话 */
    private String receiverPhone;
    /** 收货地址 */
    private String receiverAddress;
}
