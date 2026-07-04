package com.itheima.mes1.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warehouse_location")
public class WarehouseLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private String code;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
}
