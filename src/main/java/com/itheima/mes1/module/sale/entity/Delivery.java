package com.itheima.mes1.module.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("delivery")
public class Delivery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deliveryNo;
    private Long orderId;
    private Long customerId;
    private LocalDate deliveryDate;
    private Integer status;
    private String remark;
    @TableLogic
    private Integer deleted;
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private List<DeliveryItem> items;
}
