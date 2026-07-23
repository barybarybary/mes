package com.itheima.mes1.module.portal.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceOrderReq {
    private List<OrderItemReq> items;
    private String remark;

    @Data
    public static class OrderItemReq {
        private Long productId;
        private Integer quantity;
    }
}
