package com.itheima.mes1.module.base.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String spec;
    private String unit;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String imageUrl;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<com.itheima.mes1.module.base.entity.Bom> bomList;
}
