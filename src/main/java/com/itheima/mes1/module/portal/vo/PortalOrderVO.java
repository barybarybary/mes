package com.itheima.mes1.module.portal.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PortalOrderVO {
    private Long id;
    private String orderNo;
    private LocalDate orderDate;
    private Integer status;
    private String statusText;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createTime;
    private String deliveryNo;
    private LocalDate deliveryDate;
    private Integer paid;         // 0=未付款 1=已付款
    private List<PortalOrderItemVO> items;

    @Data
    public static class PortalOrderItemVO {
        private Long productId;
        private String productName;
        private String productCode;
        private String imageUrl;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal price;
        private BigDecimal amount;
    }
}
