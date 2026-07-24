package com.itheima.mes1.module.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_notification")
public class OrderNotification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String customerName;
    private BigDecimal totalAmount;
    private Integer isRead;       // 0=未读 1=已读
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
