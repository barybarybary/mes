package com.itheima.mes1.module.system.dto;

import lombok.Data;

@Data
public class SysRoleUpdateReq {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
}
