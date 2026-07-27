package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qc_standard")
public class QcStandard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long processId;
    private String itemName;
    private String itemType;
    private String unit;
    private BigDecimal specLower;
    private BigDecimal specUpper;
    private BigDecimal specTarget;
    private Integer isCritical;
    private Integer sortOrder;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String processName;
}
