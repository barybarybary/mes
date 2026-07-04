package com.itheima.mes1.module.sale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sale_order_item")
public class SaleOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal deliveredQty;
    private String remark;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
}
