package com.itheima.mes1.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory_transaction")
public class InventoryTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long warehouseId;
    private String batchNo;
    private String type;
    private BigDecimal quantity;
    private BigDecimal beforeQty;
    private BigDecimal afterQty;
    private String orderNo;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
}
