package com.itheima.mes1.module.sale.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleOrderUpdateReq {
    private Long id;
    private LocalDate deliveryDate;
    private String remark;
    private List<SaleOrderItemReq> items;
}
