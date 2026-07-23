package com.itheima.mes1.module.system.dto;

import lombok.Data;

@Data
public class SysUserCreateReq {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
