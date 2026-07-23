package com.itheima.mes1.module.sale.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryVO {
    private Long id;
    private String deliveryNo;
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String orderNo;
    private LocalDate deliveryDate;
    private Integer status;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<DeliveryItemVO> items;
}
