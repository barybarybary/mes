package com.itheima.mes1.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_alert")
public class StockAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal currentQty;
    private BigDecimal thresholdQty;
    private Integer status;       // 0=未处理 1=已处理
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime resolveTime;
}
