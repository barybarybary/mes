package com.itheima.mes1.module.portal.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductCatalogVO {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private BigDecimal price;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    /** 库存总量 */
    private Integer stockQuantity;
}
