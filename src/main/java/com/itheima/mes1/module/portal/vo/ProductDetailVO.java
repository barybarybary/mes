package com.itheima.mes1.module.portal.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDetailVO {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private BigDecimal price;
    private String imageUrl;
    private String remark;
    private String categoryName;
    private Integer stockQuantity;
}
