package com.itheima.mes1.module.system.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysRoleVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
