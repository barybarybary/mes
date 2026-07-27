package com.itheima.mes1.module.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("process_sop")
public class ProcessSop {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long processId;
    private Long kbDocumentId;
    private String sopType;
    private Integer sortOrder;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String processName;
    @TableField(exist = false)
    private String docTitle;
}
