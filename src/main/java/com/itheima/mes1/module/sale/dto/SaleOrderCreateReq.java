package com.itheima.mes1.module.sale.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleOrderCreateReq {
    private Long customerId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String remark;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private List<SaleOrderItemReq> items;
}
