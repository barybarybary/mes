package com.itheima.mes1.module.sale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("delivery_item")
public class DeliveryItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deliveryId;
    private Long productId;
    private BigDecimal quantity;
    private String batchNo;
    private String remark;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
}
