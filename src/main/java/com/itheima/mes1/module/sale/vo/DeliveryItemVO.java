package com.itheima.mes1.module.sale.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeliveryItemVO {
    private Long id;
    private Long deliveryId;
    private Long productId;
    private BigDecimal quantity;
    private String batchNo;
    private String remark;
    private String productName;
    private String productCode;
}
