package com.itheima.mes1.module.sale.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderItemVO {
    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal deliveredQty;
    private String remark;
    private String productName;
    private String productCode;
}
