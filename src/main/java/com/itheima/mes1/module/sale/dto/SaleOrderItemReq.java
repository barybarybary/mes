package com.itheima.mes1.module.sale.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderItemReq {
    private Long productId;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private String remark;
}
