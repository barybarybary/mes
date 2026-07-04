package com.itheima.mes1.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Long locationId;
    private String batchNo;
    private BigDecimal quantity;
    private BigDecimal lockedQty;
    private String unit;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String locationName;
}
