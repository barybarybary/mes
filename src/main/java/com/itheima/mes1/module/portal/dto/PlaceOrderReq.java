package com.itheima.mes1.module.portal.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PlaceOrderReq {
    private List<OrderItemReq> items;
    private String remark;
    private String receiverName;      // 收货人
    private String receiverPhone;     // 收货电话
    private String receiverAddress;   // 收货地址
    private LocalDate deliveryDate;   // 期望交期

    @Data
    public static class OrderItemReq {
        private Long productId;
        private Integer quantity;
        private java.math.BigDecimal price;  // 前端购物车已有价格，直接传过来兜底
    }
}
