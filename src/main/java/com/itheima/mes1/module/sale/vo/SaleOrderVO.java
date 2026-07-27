package com.itheima.mes1.module.sale.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleOrderVO {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private Integer status;
    private BigDecimal totalAmount;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private List<SaleOrderItemVO> items;
}
